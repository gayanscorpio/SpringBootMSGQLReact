package com.spring.auth.aws.resolver;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;

import com.spring.auth.aws.model.AppUser;
import com.spring.auth.aws.model.AuthResponse;
import com.spring.auth.aws.repository.UserRepository;
import com.spring.auth.aws.util.JwtUtil;

@Controller
public class AuthMutationResolver {

	@Autowired
	private UserRepository userRepo;
	@Autowired
	private JwtUtil jwtUtil;
	private final PasswordEncoder encoder = new BCryptPasswordEncoder();

	@MutationMapping
	public Boolean register(@Argument String username, @Argument String password) {

		System.out.println("<<<<<<<<<<<<<<<< user register calling ... <<<<<<<<<<<<");
		if (userRepo.findByUsername(username).isPresent()) {
			throw new RuntimeException("User already exists");
		}

		AppUser user = new AppUser();
		user.setUsername(username);
		user.setPassword(encoder.encode(password));
		user.setRole("Admin");
		userRepo.save(user);

		System.out.println("<<<<<<<<<<<<<<<< user registered successfully <<<<<<<<<<<<");

		return true;
	}

	@MutationMapping
	public AuthResponse login(@Argument String username, @Argument String password) {
		AppUser user = userRepo.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));

		if (!encoder.matches(password, user.getPassword())) {
			throw new RuntimeException("Invalid credentials");
		}

		String token = jwtUtil.generateToken(user);
		return new AuthResponse(token, user.getId());
	}
}
