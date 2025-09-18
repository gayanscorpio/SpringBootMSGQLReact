package com.spring.book.aws.subcription;

import org.reactivestreams.Publisher;
import reactor.core.publisher.Sinks;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsSubscription;
import com.spring.book.aws.model.Book;

/**
 * Subscription Resolver (using Sinks API), Any GraphQL client with an active
 * subscription gets this new Book pushed to them.
 * 
 * This is the bridge between Redis Pub/Sub and GraphQL subscriptions.
 * 
 * BookSubscriptionResolver → Exposes GraphQL bookAdded subscription; emits Book
 * events to clients.
 */
@DgsComponent
public class BookSubscriptionResolver {

	private final Sinks.Many<Book> sink = Sinks.many().multicast().onBackpressureBuffer();

	/**
	 * Exposes GraphQL bookAdded subscription; emits Book events to clients.
	 * 
	 * @return
	 */
	@DgsSubscription
	public Publisher<Book> bookAdded() {

		return sink.asFlux();
	}

	public void publishBookAdded(Book book) {
		sink.tryEmitNext(book);
	}
}
