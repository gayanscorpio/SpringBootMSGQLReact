package com.spring.auth.aws.resolver;

import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Component;

@Component
public class QueryResolver {

	@QueryMapping
	public String ping() {
		return "pong";
	}
}
