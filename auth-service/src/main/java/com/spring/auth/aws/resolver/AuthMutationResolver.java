package com.spring.auth.aws.resolver;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsMutation;
import com.netflix.graphql.dgs.DgsQuery;
import com.netflix.graphql.dgs.InputArgument;
import com.spring.auth.aws.model.AppUser;
import com.spring.auth.aws.repository.UserRepository;
import com.spring.auth.aws.util.JwtUtil;

@DgsComponent
public class AuthMutationResolver {

	@Autowired
	private UserRepository userRepo;
	@Autowired
	private JwtUtil jwtUtil;
	private final PasswordEncoder encoder = new BCryptPasswordEncoder();

	/**
	 * A record is a special type in Java that is meant to be a data carrier —
	 * basically a class that only contains fields (data) and automatically
	 * generates:private final fields
	 * 
	 * Constructor Getters (token(), userId(), etc.) equals(), hashCode() toString()
	 */
	public record AuthResponse(String token, Long userId, String role, Integer age, Boolean isAdult) {
	}

	@DgsMutation
	public Boolean register(@InputArgument String username, @InputArgument String password,
			@InputArgument String role) {

		System.out.println("<<<<<<<<<<<<<<<< user register calling ... <<<<<<<<<<<<");
		if (userRepo.findByUsername(username).isPresent()) {
			throw new RuntimeException("User already exists");
		}

		AppUser user = new AppUser();
		user.setUsername(username);
		user.setPassword(encoder.encode(password));
		user.setRole(role != null ? role : "User");
		user.setOtpCode("000000");
		user.setPhone("076 000 0000");
		userRepo.save(user);

		System.out.println("<<<<<<<<<<<<<<<< user registered successfully <<<<<<<<<<<<");

		return true;
	}

	@DgsMutation
	public AuthResponse login(@InputArgument String username, @InputArgument String password) {
		AppUser user = userRepo.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));

		if (!encoder.matches(password, user.getPassword())) {
			throw new RuntimeException("Invalid credentials");
		}

		String token = jwtUtil.generateToken(user);
		Integer age = user.getAge();
		Boolean isAdult = (age != null && age >= 18);
		return new AuthResponse(token, user.getId(), user.getRole(), user.getAge(), isAdult);
	}

	/**
	 * 🔹 New: Register with Phone (requires OTP)
	 * 
	 * @param username
	 * @param password
	 * @param phone
	 * @return
	 */
	@DgsMutation
	public boolean registerWithPhone(@InputArgument String username, @InputArgument String password,
			@InputArgument String phone, @InputArgument String role) {
		if (userRepo.findByUsername(username).isPresent()) {
			throw new RuntimeException("Username already exists");
		}

		AppUser user = new AppUser();
		user.setUsername(username);
		user.setPassword(encoder.encode(password));
		user.setPhone(phone);
		// ✅ Default role if not provided
		user.setRole(role != null && !role.isBlank() ? role.toUpperCase() : "USER");

		// Generate OTP
		String otp = String.format("%06d", new Random().nextInt(999999));
		user.setOtpCode(otp);
		user.setOtpExpiry(LocalDateTime.now().plusMinutes(5));

		// TODO: Replace with Twilio / AWS SNS
		System.out.println("📲 OTP for " + phone + ": " + otp);

		userRepo.save(user);
		return true;
	}

	/**
	 * 🔹 New: Verify phone and issue JWT
	 * 
	 * @param username
	 * @param code
	 * @return
	 */
	@DgsMutation
	public AuthResponse verifyPhone(@InputArgument String username, @InputArgument String code) {
		AppUser user = userRepo.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));

		if (user.getOtpCode() == null || user.getOtpExpiry().isBefore(LocalDateTime.now())) {
			throw new RuntimeException("OTP expired or invalid");
		}

		if (!user.getOtpCode().equals(code)) {
			throw new RuntimeException("Invalid OTP");
		}

		user.setPhoneVerified(true);
		user.setOtpCode(user.getOtpCode());
		user.setOtpExpiry(null);
		userRepo.save(user);

		// ✅ Issue a real JWT token (same as login)
		String token = jwtUtil.generateToken(user);

		Integer age = user.getAge();
		Boolean isAdult = (age != null && age >= 18);

		// ✅ Return full AuthResponse structure
		return new AuthResponse(token, user.getId(), user.getRole(), age, isAdult);
	}

	@DgsMutation
	public boolean registerWithDob(@InputArgument String username, @InputArgument String password,
			@InputArgument String phone, @InputArgument String role, @InputArgument String dob) {

		if (userRepo.findByUsername(username).isPresent()) {
			throw new RuntimeException("Username already exists");
		}

		LocalDate birthDate = LocalDate.parse(dob);
		int age = Period.between(birthDate, LocalDate.now()).getYears();

		AppUser user = new AppUser();
		user.setUsername(username);
		user.setPassword(encoder.encode(password));
		user.setPhone(phone);
		user.setRole(role);
		user.setDob(LocalDate.parse(dob));

		// ✅ Generate OTP
		String otp = String.format("%06d", new Random().nextInt(999999));
		user.setOtpCode(otp);
		user.setOtpExpiry(LocalDateTime.now().plusMinutes(5));
		user.setPhoneVerified(false);
		user.setAge(age);

		// Optionally reject underage registration
		if (age < 13) {
			throw new RuntimeException("You must be 13 or older to register.");
		}

		// For now, print OTP in console (until you integrate Twilio/SMS)
		System.out.println("📲 OTP for " + phone + ": " + otp);
		System.out.println("✅ User registered: " + username + " (Age: " + age + ")");

		userRepo.save(user);
		return true;
	}

}
