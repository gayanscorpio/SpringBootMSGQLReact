package com.spring.book.aws.datafetcher;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsData;
import com.netflix.graphql.dgs.DgsDataFetchingEnvironment;
import com.netflix.graphql.dgs.DgsEntityFetcher;
import com.netflix.graphql.dgs.DgsMutation;
import com.netflix.graphql.dgs.DgsQuery;
import com.netflix.graphql.dgs.InputArgument;
import com.spring.book.aws.model.Book;
import com.spring.book.aws.model.Student;
import com.spring.book.aws.repository.BookRepository;

import jakarta.annotation.PostConstruct;

@DgsComponent
public class BookMutationResolver {

	@PostConstruct
	public void logSchemaLoaded() {
		System.out.println("✅ DGS schema loaded successfully!");
	}

	private final BookRepository bookRepository;

	public BookMutationResolver(BookRepository bookRepository) {
		this.bookRepository = bookRepository;
	}

	@DgsMutation
	public Book addBook(@InputArgument String title, @InputArgument String author, @InputArgument int totalCopies) {
		Book newBook = new Book(UUID.randomUUID().toString(), title, author, totalCopies, totalCopies, null);
		return bookRepository.save(newBook);
	}

	@DgsMutation
	public Book updateBook(@InputArgument String id, @InputArgument String title, @InputArgument String author,
			@InputArgument int totalCopies) {
		Book book = bookRepository.findById(id).orElseThrow(() -> new RuntimeException("Book not found"));
		book.setTitle(title);
		book.setAuthor(author);
		book.setTotalCopies(totalCopies);
		return bookRepository.save(book);
	}

	@DgsMutation
	public Boolean deleteBook(@InputArgument String id) {
		bookRepository.deleteById(id);
		return true;
	}

	@DgsQuery
	public List<Book> allBooks() {
		return bookRepository.findAll();
	}

	/**
	 * borrowedBy resolver returns a stub Student instance with just the ID—Apollo
	 * Gateway handles fetching the rest.
	 * 
	 * @param dfe
	 * @return
	 */
	@DgsData(parentType = "Book", field = "borrowedBy")
	public Student borrowedBy(DgsDataFetchingEnvironment dfe) {
		Book book = dfe.getSource();
		if (book.getBorrowedStudentId() == null)
			return null;
		// Stub Student entity with only ID
		return new Student(book.getBorrowedStudentId());
	}

	@DgsData(parentType = "Student", field = "borrowedBooksCount")
	public Integer borrowedBooksCount(DgsDataFetchingEnvironment dfe) {
		Student student = dfe.getSource(); // federated Student stub
		if (student.getId() == null)
			return 0;
		return bookRepository.countByBorrowedStudentId(student.getId());
	}

	@DgsMutation
	public Book borrowBook(@InputArgument String bookId, @InputArgument String studentId) {
		Book book = bookRepository.findById(bookId).orElseThrow(() -> new RuntimeException("Book not found"));

		if (book.getAvailableCopies() <= 0) {
			throw new RuntimeException("No available copies");
		}

		book.setAvailableCopies(book.getAvailableCopies() - 1);
		book.setBorrowedStudentId(studentId);
		return bookRepository.save(book);
	}

	@DgsMutation
	public Book returnBook(@InputArgument String bookId) {
		Book book = bookRepository.findById(bookId).orElseThrow(() -> new RuntimeException("Book not found"));

		book.setAvailableCopies(book.getAvailableCopies() + 1);
		book.setBorrowedStudentId(null);
		return bookRepository.save(book);
	}

	@DgsEntityFetcher(name = "Student")
	public Student studentEntityFetcher(Map<String, Object> values) {
		String id = (String) values.get("id");
		return new Student(id); // just stub, gateway will call Student service for full data
	}

}
