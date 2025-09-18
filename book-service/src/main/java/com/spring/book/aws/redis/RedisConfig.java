package com.spring.book.aws.redis;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis Server → Broadcasts event to all subscribers.
 */
@Configuration
public class RedisConfig {

	@Bean
	public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
		RedisTemplate<String, Object> template = new RedisTemplate<>();
		template.setConnectionFactory(factory);

		// Key serializer
		template.setKeySerializer(new StringRedisSerializer());

		// Value serializer
		template.setValueSerializer(new Jackson2JsonRedisSerializer<>(Object.class));

		return template;
	}

	@Bean
	public ChannelTopic topic() {
		return new ChannelTopic("book-events");
	}

	@Bean
	public RedisMessageListenerContainer redisContainer(RedisConnectionFactory connectionFactory,
			MessageListenerAdapter listenerAdapter, ChannelTopic topic) {
		RedisMessageListenerContainer container = new RedisMessageListenerContainer();
		container.setConnectionFactory(connectionFactory);
		container.addMessageListener(listenerAdapter, topic);
		return container;
	}

	@Bean
	public MessageListenerAdapter listenerAdapter(BookRedisSubscriber subscriber) {
		// "onMessage" must match the method name in BookRedisSubscriber
		return new MessageListenerAdapter(subscriber, "onMessage");
	}
}
