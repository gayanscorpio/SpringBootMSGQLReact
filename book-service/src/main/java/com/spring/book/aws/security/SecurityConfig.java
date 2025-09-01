package com.spring.book.aws.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

		System.out.println("<<<<<<<<<<<<<<<< securityFilterChain <<<<<<<<<<<<");
		http.csrf(csrf -> csrf.disable()).authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
				.httpBasic(httpBasic -> httpBasic.disable()).formLogin(form -> form.disable());

		return http.build();
	}
}
