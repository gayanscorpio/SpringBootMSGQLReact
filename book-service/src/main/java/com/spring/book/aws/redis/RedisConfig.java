package com.spring.book.aws.redis;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.spring.book.aws.model.Book;

/**
 * Redis Server → Broadcasts event to all subscribers.
 */
@Configuration
public class RedisConfig {

	@Bean
	public RedisTemplate<String, Book> redisTemplate(RedisConnectionFactory factory) {
		RedisTemplate<String, Book> template = new RedisTemplate<>();
		template.setConnectionFactory(factory);

		// Key serializer
		template.setKeySerializer(new StringRedisSerializer());

		// Value serializer: serialize/deserialize Book directly
		Jackson2JsonRedisSerializer<Book> serializer = new Jackson2JsonRedisSerializer<>(Book.class);

		// Value serializer
		template.setValueSerializer(serializer);
		template.setHashValueSerializer(serializer);

		template.afterPropertiesSet();

		return template;
	}

	@Bean
	public ChannelTopic topic() {
		return new ChannelTopic("BOOK_ADDED");
	}

	@Bean
	public RedisMessageListenerContainer redisContainer(RedisConnectionFactory connectionFactory,
			BookRedisSubscriber subscriber, // inject the subscriber directly
			ChannelTopic topic) {
		RedisMessageListenerContainer container = new RedisMessageListenerContainer();
		container.setConnectionFactory(connectionFactory);

		// Add the subscriber directly
		container.addMessageListener(subscriber, topic);
		return container;
	}

}
