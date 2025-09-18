package com.spring.book.aws.redis;

import org.springframework.data.redis.connection.Message;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring.book.aws.model.Book;
import com.spring.book.aws.subcription.BookSubscriptionResolver;

/**
 * BookRedisSubscriber → Listens for Redis messages, converts JSON → Book,
 * pushes to sink.
 */
@Component
public class BookRedisSubscriber {

	private final BookSubscriptionResolver bookSubscriptionResolver;
	private final ObjectMapper mapper = new ObjectMapper();

	// Constructor
	public BookRedisSubscriber(BookSubscriptionResolver bookSubscriptionResolver) {
		this.bookSubscriptionResolver = bookSubscriptionResolver;
	}

	// ✅ Must be public and have exactly these parameters
	public void onMessage(String message) {
		System.out.println(" xxxxxxxxxxxxxxxxxxxx  - onMessage :" + message);
		try {
			Book book = mapper.readValue(message, Book.class);

			// Redis message is converted back into a Book object and passed into the
			// GraphQL subscription sink.
			bookSubscriptionResolver.publishBookAdded(book);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
