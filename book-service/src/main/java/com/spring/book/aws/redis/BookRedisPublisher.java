package com.spring.book.aws.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring.book.aws.model.Book;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BookRedisPublisher {

	private static final Logger logger = LoggerFactory.getLogger(BookRedisPublisher.class);
	
	public static final String BOOK_ADDED_CHANNEL = "BOOK_ADDED";

	private final RedisTemplate<String, Book> redisTemplate;

	public void publish(Book book) {
		logger.info("Publishing book ID: {}, Title: {} to channel {}", book.getId(), book.getTitle(),
				BOOK_ADDED_CHANNEL);
		try {
			redisTemplate.convertAndSend(BOOK_ADDED_CHANNEL, book);
			logger.info("Successfully published book: {}", book);
		} catch (Exception e) {
			logger.error("Failed to publish book: {}", book, e);
		}
	}

	public void publish(String channel, Book book) {
		try {
			redisTemplate.convertAndSend(channel, book);
			logger.info("Successfully published {} event: {}", channel, book);
		} catch (Exception e) {
			logger.error("Failed to publish {} event for book: {}", channel, book, e);
		}
	}

}
