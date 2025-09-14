package com.spring.book.aws.exception;

import jakarta.persistence.OptimisticLockException;
import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter;
import org.springframework.stereotype.Component;
import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.schema.DataFetchingEnvironment;

@Component
public class GraphQLExceptionHandler extends DataFetcherExceptionResolverAdapter {

	@Override
	protected GraphQLError resolveToSingleError(Throwable ex, DataFetchingEnvironment env) {
		if (ex instanceof OptimisticLockException) {
			return GraphqlErrorBuilder.newError(env)
					.message("Another user is updating this book right now. Please try again.").build();
		}
		// fallback
		return GraphqlErrorBuilder.newError(env).message("Unexpected error: " + ex.getMessage()).build();
	}
}
