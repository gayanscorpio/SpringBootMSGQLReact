package com.spring.book.aws.datafetcher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.prepost.PreAuthorize;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsData;
import com.netflix.graphql.dgs.DgsDataFetchingEnvironment;
import com.netflix.graphql.dgs.DgsEntityFetcher;
import com.netflix.graphql.dgs.DgsMutation;
import com.netflix.graphql.dgs.DgsQuery;
import com.netflix.graphql.dgs.InputArgument;
import com.spring.book.aws.model.Book;
import com.spring.book.aws.model.Student;
import com.spring.book.aws.redis.BookRedisPublisher;
import com.spring.book.aws.repository.BookRepository;
import com.spring.book.aws.subcription.BookSubscriptionResolver;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.OptimisticLockException;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;

@DgsComponent
@AllArgsConstructor
public class BookMutationResolver {

	@PostConstruct
	public void logSchemaLoaded() {
		System.out.println("✅ DGS schema loaded successfully!");
	}

	private final BookRepository bookRepository;
	private final BookRedisPublisher redisPublisher;

	@DgsMutation
	@PreAuthorize("hasRole('Admin')") // Only Admins can add books
	public Book addBook(@InputArgument String title, @InputArgument String author, @InputArgument int totalCopies) {
		Book book = new Book();
		book.setId(UUID.randomUUID().toString());
		book.setTitle(title); // MUST NOT BE NULL
		book.setAuthor(author); // MUST NOT BE NULL
		book.setTotalCopies(totalCopies);
		book.setAvailableCopies(totalCopies);
		book.setBorrowedStudentIds(new ArrayList<>());
		Book saved = bookRepository.save(book);

		// 🔔 Publish to Redis so all instances broadcast
		// Publishes event to Redis using BookRedisPublisher.
		redisPublisher.publish(saved);
		return saved;
	}

	@DgsMutation
	@PreAuthorize("hasRole('Admin')") // Only Admins can update books
	public Book updateBook(@InputArgument String id, @InputArgument String title, @InputArgument String author,
			@InputArgument int totalCopies) {
		Book book = bookRepository.findById(id).orElseThrow(() -> new RuntimeException("Book not found"));

		int borrowedCount = book.getBorrowedStudentIds().size();
		if (totalCopies < borrowedCount) {
			throw new RuntimeException("Total copies cannot be less than borrowed copies");
		}

		int diff = totalCopies - book.getTotalCopies();
		book.setTitle(title);
		book.setAuthor(author);
		book.setTotalCopies(totalCopies);
		book.setAvailableCopies(book.getAvailableCopies() + diff); // adjust available copies
		return bookRepository.save(book);
	}

	@DgsMutation
	@PreAuthorize("hasRole('Admin')") // Only Admins can delete books
	public Boolean deleteBook(@InputArgument String id) {
		bookRepository.deleteById(id);
		return true;
	}

	@DgsQuery
	@PreAuthorize("hasAnyRole('Admin', 'Student')") // allow both Admin and Student
	public List<Book> allBooks() {
		return bookRepository.findAll();
	}

	/**
	 * For each book, GraphQL sees a request for borrowedBy →
	 * calls @DgsData(parentType="Book", field="borrowedBy"). Important: This is not
	 * the full Student yet. The Apollo Gateway knows that Student is a federated
	 * entity, so it calls the Student service to fetch full name and email.
	 * 
	 * @param dfe
	 * @return
	 */
	@DgsData(parentType = "Book", field = "borrowedBy")
	public List<Map<String, String>> borrowedBy(DgsDataFetchingEnvironment dfe) {
		Book book = dfe.getSource();

		if (book.getBorrowedStudentIds() == null || book.getBorrowedStudentIds().isEmpty()) {
			return List.of();
		}

		// Return only ID references for Apollo Federation
		return book.getBorrowedStudentIds().stream().map(id -> Map.of("id", id)).collect(Collectors.toList());
	}

	/**
	 * Resolves the borrowedBooksCount field for a Student. Counts how many books in
	 * BookRepository have borrowedStudentId = student.id. extend type Student :
	 * "Student" is the GraphQL type
	 * 
	 * @param dfe
	 * @return
	 */
	@DgsData(parentType = "Student", field = "borrowedBooksCount")
	public Integer borrowedBooksCount(DgsDataFetchingEnvironment dfe) {
		Student student = dfe.getSource(); // federated Student stub
		if (student.getId() == null)
			return 0;
		return bookRepository.countByBorrowedStudentId(student.getId());
	}

	// --- Borrow / Return with Optimistic Locking ---
	@Transactional
	@DgsMutation
	public Book borrowBook(@InputArgument String bookId, @InputArgument String studentId) {
		try {
			Book book = bookRepository.findById(bookId).orElseThrow(() -> new RuntimeException("Book not found"));

			if (book.getAvailableCopies() <= 0) {
				throw new RuntimeException("No available copies");
			}
			// force initialize lazy collection
			if (book.getBorrowedStudentIds() == null) {
				book.setBorrowedStudentIds(new ArrayList<>());
			} else {
				book.getBorrowedStudentIds().size(); // touch collection inside transaction
			}

			if (book.getBorrowedStudentIds().contains(studentId)) {
				throw new RuntimeException("Student has already borrowed this book");
			}

			book.getBorrowedStudentIds().add(studentId);
			book.setAvailableCopies(book.getTotalCopies() - book.getBorrowedStudentIds().size());

			return bookRepository.saveAndFlush(book);
		} catch (OptimisticLockException | ObjectOptimisticLockingFailureException e) {
			throw new RuntimeException("⚠️ Book is being borrowed by another student right now. Please try again.", e);
		}

	}

	// --- Borrow / Return with Optimistic Locking ---
	@Transactional
	@DgsMutation
	public Book returnBook(@InputArgument String bookId, @InputArgument String studentId) {
		try {
			Book book = bookRepository.findById(bookId).orElseThrow(() -> new RuntimeException("Book not found"));

			if (book.getBorrowedStudentIds() == null || !book.getBorrowedStudentIds().contains(studentId)) {
				throw new RuntimeException("Student has not borrowed this book");
			}
			book.getBorrowedStudentIds().remove(studentId);
			book.setAvailableCopies(book.getTotalCopies() - book.getBorrowedStudentIds().size());
			return bookRepository.saveAndFlush(book);
		} catch (OptimisticLockException e) {
			throw new RuntimeException("⚠️ Book is being updated by another student right now. Please try again.", e);
		}
	}

	/**
	 * Entity Fetcher for Federation: This allows Book service to resolve a Student
	 * entity using only the id. Apollo Gateway then queries Student service to get
	 * full data (name, email).
	 */
	@DgsEntityFetcher(name = "Student")
	public Student studentEntityFetcher(Map<String, Object> values) {
		String id = (String) values.get("id");
		return new Student(id); // just stub, gateway will call Student service for full data
	}

	@DgsData(parentType = "Student", field = "borrowedBooks")
	public List<Book> getStudentWithBorrowedBooks(DgsDataFetchingEnvironment dfe) {
		Student student = dfe.getSource(); // the Student entity (stub with id).
		List<Book> books = bookRepository.findByBorrowedStudentId(student.getId());
		return books != null ? books : Collections.emptyList(); // ✅ always a list
	}

}
