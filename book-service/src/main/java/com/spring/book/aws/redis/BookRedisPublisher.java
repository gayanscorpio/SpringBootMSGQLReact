package com.spring.book.aws.redis;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;

import com.spring.book.aws.model.Book;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BookRedisPublisher {

	private final RedisTemplate<String, Object> redisTemplate;
	private final ChannelTopic topic;

	public void publish(Book book) {
		// redisTemplate.convertAndSend("book-events", book) to publish to Redis.
		// Redis now broadcasts this event to all subscribers listening on the
		// book-events channel.
		redisTemplate.convertAndSend(topic.getTopic(), book);
	}
}
