package com.spring.book.aws.datafetcher;

import java.util.List;
import java.util.UUID;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsData;
import com.netflix.graphql.dgs.DgsDataFetchingEnvironment;
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
		return new Student(book.getBorrowedStudentId());
	}
}
