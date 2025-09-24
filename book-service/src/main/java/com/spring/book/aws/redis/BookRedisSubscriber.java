package com.spring.book.aws.redis;

import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring.book.aws.model.Book;
import com.spring.book.aws.subcription.BookSubscriptionResolver;

/**
 * BookRedisSubscriber → Listens for Redis messages, converts JSON → Book,
 * pushes to sink.
 */
@Component
public class BookRedisSubscriber implements MessageListener {

	private static final Logger logger = LoggerFactory.getLogger(BookRedisSubscriber.class);

	private final BookSubscriptionResolver bookSubscriptionResolver;

	// Constructor
	public BookRedisSubscriber(BookSubscriptionResolver bookSubscriptionResolver) {
		this.bookSubscriptionResolver = bookSubscriptionResolver;
	}

	// ✅ Must be public and have exactly these parameters
	@Override
	public void onMessage(Message message, byte[] pattern) {
		try {

			// Deserialize using Jackson2JsonRedisSerializer
			Jackson2JsonRedisSerializer<Book> serializer = new Jackson2JsonRedisSerializer<>(Book.class);
			Book book = serializer.deserialize(message.getBody());

			logger.info("📢 Parsed Book object: id={}, title={}", book.getId(), book.getTitle());
			bookSubscriptionResolver.publishBookAdded(book);

		} catch (Exception e) {
			logger.error("❌ Failed to parse Redis message into Book object", e);
		}
	}

}
