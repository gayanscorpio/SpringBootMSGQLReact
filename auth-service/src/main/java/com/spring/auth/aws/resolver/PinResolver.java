package com.spring.auth.aws.resolver;

import java.time.LocalDateTime;

import org.springframework.security.crypto.password.PasswordEncoder;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsMutation;
import com.netflix.graphql.dgs.DgsQuery;
import com.netflix.graphql.dgs.InputArgument;
import com.spring.auth.aws.dto.PinResponse;
import com.spring.auth.aws.model.UserPin;
import com.spring.auth.aws.repository.UserPinRepository;

import lombok.RequiredArgsConstructor;

@DgsComponent
@RequiredArgsConstructor
public class PinResolver {

	private final UserPinRepository repository;
	private final PasswordEncoder passwordEncoder;

	@DgsMutation
	public PinResponse setUserPin(@InputArgument("input") SetPinInput input) {
		String userId = input.getUserId();
		String pin = input.getPin();

		String hashed = passwordEncoder.encode(pin);
		UserPin userPin = new UserPin(userId, hashed, 0, null, LocalDateTime.now());
		repository.save(userPin);

		return new PinResponse(true, "PIN set successfully");
	}

	@DgsMutation
	public PinResponse verifyUserPin(@InputArgument("input") VerifyPinInput input) {
		String userId = input.getUserId();
		String pin = input.getPin();

		UserPin userPin = repository.findById(userId).orElse(null);

		if (userPin == null) {
			return new PinResponse(false, "PIN not set");
		}

		if (userPin.getLockedUntil() != null && userPin.getLockedUntil().isAfter(LocalDateTime.now())) {
			return new PinResponse(false, "PIN temporarily locked");
		}

		if (passwordEncoder.matches(pin, userPin.getPinHash())) {
			userPin.setFailedAttempts(0);
			repository.save(userPin);
			return new PinResponse(true, "PIN verified");
		} else {
			userPin.setFailedAttempts(userPin.getFailedAttempts() + 1);
			if (userPin.getFailedAttempts() >= 3) {
				userPin.setLockedUntil(LocalDateTime.now().plusMinutes(15));
			}
			repository.save(userPin);
			return new PinResponse(false, "Incorrect PIN");
		}
	}

	@DgsQuery
	public PinResponse checkUserPin(@InputArgument String userId) {
		UserPin userPin = repository.findById(userId).orElse(null);

		if (userPin == null) {
			return new PinResponse(false, "PIN not set");
		}

		return new PinResponse(true, "PIN exists");
	}

	// Input types
	public static class SetPinInput {
		private String userId;
		private String pin;

		public String getUserId() {
			return userId;
		}

		public void setUserId(String userId) {
			this.userId = userId;
		}

		public String getPin() {
			return pin;
		}

		public void setPin(String pin) {
			this.pin = pin;
		}
	}

	public static class VerifyPinInput {
		private String userId;
		private String pin;

		public String getUserId() {
			return userId;
		}

		public void setUserId(String userId) {
			this.userId = userId;
		}

		public String getPin() {
			return pin;
		}

		public void setPin(String pin) {
			this.pin = pin;
		}
	}
}
