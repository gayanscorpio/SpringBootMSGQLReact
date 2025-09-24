package com.spring.book.aws.subcription;

import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import reactor.core.publisher.Sinks;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsSubscription;
import com.spring.book.aws.model.Book;

/**
 * Subscription Resolver (using Reactor Sinks API).
 * 
 * Any GraphQL client with an active subscription gets this new Book pushed to
 * them. Acts as a bridge between Redis Pub/Sub and GraphQL subscriptions.
 */
@DgsComponent
public class BookSubscriptionResolver {

	private static final Logger logger = LoggerFactory.getLogger(BookSubscriptionResolver.class);

	// Replay sink ensures that the latest Book is cached for new subscribers
	private final Sinks.Many<Book> sink = Sinks.many().replay().latest();

	/**
	 * GraphQL subscription: bookAdded Exposes a stream of books as they're
	 * published.
	 */
	@DgsSubscription
	public Publisher<Book> bookAdded() {
		logger.info("📡 Subscription started: bookAdded");
		return sink.asFlux();
	}

	/**
	 * Publish a new Book event to all subscribers.
	 */
	public void publishBookAdded(Book book) {
		logger.info("📢 Publishing new book to subscribers: {}", book);
		Sinks.EmitResult result = sink.tryEmitNext(book);

		if (result.isFailure()) {
			logger.warn("⚠️ Failed to emit book {}: {}", book.getId(), result);
		}
	}
}
