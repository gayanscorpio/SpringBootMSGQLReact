package com.spring.book.aws

import com.spring.book.aws.model.Book
import com.spring.book.aws.repository.BookRepository
import jakarta.persistence.OptimisticLockException
import com.spring.book.aws.datafetcher.BookMutationResolver
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.orm.ObjectOptimisticLockingFailureException
import org.springframework.transaction.annotation.Transactional
import org.springframework.test.annotation.Rollback
import org.springframework.test.context.ContextConfiguration

import spock.lang.Specification

import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors

// Explicitly point to main Spring Boot class
@SpringBootTest(classes = com.spring.book.aws.BookServiceApplication)
@ContextConfiguration(classes = com.spring.book.aws.BookServiceApplication)
class BookConcurrencySpec extends Specification {

	@Autowired
	BookRepository bookRepository

	@Autowired
	BookMutationResolver bookMutationResolver

	def "test concurrent borrow of the same book"() {
		given: "A book with 1 available copy"
		Book book = new Book()
		book.title = "Concurrent Groovy Book"
		book.author = "Test Author"
		book.totalCopies = 1
		book.availableCopies = 1
		book.borrowedStudentIds = []
		book = bookRepository.saveAndFlush(book)

		and: "Two student IDs trying to borrow at the same time"
		String student1 = "student-1"
		String student2 = "student-2"

		CountDownLatch latch = new CountDownLatch(1)
		def executor = Executors.newFixedThreadPool(2)
		List<Exception> exceptions = []

		when: "Both students try to borrow concurrently"
		executor.submit({
			try {
				latch.await()
				bookMutationResolver.borrowBook(book.id, student1)
			} catch (Exception e) {
				exceptions << e
			}
		})

		executor.submit({
			try {
				latch.await()
				bookMutationResolver.borrowBook(book.id, student2)
			} catch (Exception e) {
				exceptions << e
			}
		})

		latch.countDown()
		executor.shutdown()
		executor.awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS)

		then: "Only one borrow should succeed"
		Book updatedBook = bookRepository.findById(book.id).get()
		updatedBook.availableCopies == 0
		updatedBook.borrowedStudentIds.size() == 1

		exceptions.size() == 1
		exceptions[0].message.contains("⚠️ Book is being borrowed")

		def cause = exceptions[0].cause
		cause instanceof ObjectOptimisticLockingFailureException || cause instanceof OptimisticLockException
	}
}
