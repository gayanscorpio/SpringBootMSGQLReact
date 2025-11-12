package com.spring.auth.aws.resolver;

import java.util.Optional;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsData;
import com.netflix.graphql.dgs.DgsDataFetchingEnvironment;
import com.netflix.graphql.dgs.DgsEntityFetcher;
import com.spring.auth.aws.model.AppUser;
import com.spring.auth.aws.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@DgsComponent
@RequiredArgsConstructor
public class UserEntityResolver {

	private final UserRepository userRepository;

	/**
	 * @DgsEntityFetcher → resolves federated entity requests from other services.
	 *                   Other MS can now fetch User by id without calling Auth MS
	 *                   directly.
	 * @param env
	 * @return
	 */
	@DgsEntityFetcher(name = "AppUser")
	public AppUser resolveUser(DgsDataFetchingEnvironment env) {
		// "id" comes from federated service
		Long userId = Long.parseLong(env.getArgument("id"));
		Optional<AppUser> userOpt = userRepository.findById(userId);
		return userOpt.orElse(null);
	}

	/**
	 * If you later want to compute fields dynamically
	 * 
	 * @param env
	 * @return
	 */
	@DgsData(parentType = "AppUser", field = "isAdult")
	public Boolean isAdult(DgsDataFetchingEnvironment env) {
		AppUser user = env.getSource();
		return user.getAge() != null && user.getAge() >= 18;
	}

}
