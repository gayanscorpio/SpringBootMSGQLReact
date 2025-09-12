package com.spring.book.aws.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity // ✅ Enables @PreAuthorize
public class SecurityConfig {

	@Autowired
	private JwtVerificationFilter jwtFilter;

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

		http.csrf(csrf -> csrf.disable()).authorizeHttpRequests(auth -> auth
				// Require authentication for ALL GraphQL requests
				.requestMatchers("/graphql").authenticated()
				// Allow actuator/health if you want monitoring
				.requestMatchers("/actuator/health").permitAll()
				// Everything else also requires auth
				.anyRequest().authenticated()).addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
				.httpBasic(httpBasic -> httpBasic.disable()).formLogin(form -> form.disable());

		return http.build();
	}
}
