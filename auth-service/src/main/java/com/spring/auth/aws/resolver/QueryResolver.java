package com.spring.auth.aws.resolver;

import org.springframework.beans.factory.annotation.Autowired;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsQuery;
import com.netflix.graphql.dgs.InputArgument;
import com.spring.auth.aws.model.AppUser;
import com.spring.auth.aws.repository.UserRepository;

@DgsComponent
public class QueryResolver {
	@Autowired
	private UserRepository userRepo;

	@DgsQuery
	public String ping() {
		return "pong";
	}

	@DgsQuery
	public AppUser appUser(@InputArgument Long id) {
		return userRepo.findById(id).orElse(null);
	}
}
