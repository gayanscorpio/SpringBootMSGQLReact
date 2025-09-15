package com.spring.book.aws

import com.spring.book.aws.datafetcher.BookMutationResolver
import com.spring.book.aws.repository.BookRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

@SpringBootTest(classes = com.spring.book.aws.BookServiceApplication)
@ContextConfiguration(classes = com.spring.book.aws.BookServiceApplication)
class TestSpec extends Specification {

	@Autowired
	BookRepository bookRepository

	@Autowired
	BookMutationResolver bookMutationResolver

	def "context loads and repo exists"() {
		expect:
		bookRepository != null
		bookMutationResolver != null
	}
}
