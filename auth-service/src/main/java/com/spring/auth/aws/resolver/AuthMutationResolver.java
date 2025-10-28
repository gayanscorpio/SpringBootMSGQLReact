package com.spring.auth.aws.resolver;

import java.time.LocalDateTime;
import java.util.Random;

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
	public Boolean register(@Argument String username, @Argument String password, @Argument String role) {

		System.out.println("<<<<<<<<<<<<<<<< user register calling ... <<<<<<<<<<<<");
		if (userRepo.findByUsername(username).isPresent()) {
			throw new RuntimeException("User already exists");
		}

		AppUser user = new AppUser();
		user.setUsername(username);
		user.setPassword(encoder.encode(password));
		user.setRole(role != null ? role : "User");
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
		return new AuthResponse(token, user.getId(), user.getRole());
	}

	/**
	 * 🔹 New: Register with Phone (requires OTP)
	 * 
	 * @param username
	 * @param password
	 * @param phone
	 * @return
	 */
	@MutationMapping
	public boolean registerWithPhone(@Argument String username, @Argument String password, @Argument String phone) {
		if (userRepo.findByUsername(username).isPresent()) {
			throw new RuntimeException("Username already exists");
		}

		AppUser user = new AppUser();
		user.setUsername(username);
		user.setPassword(encoder.encode(password));
		user.setPhone(phone);
		user.setRole("USER");

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
	@MutationMapping
	public AuthResponse verifyPhone(@Argument String username, @Argument String code) {
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

		// ✅ Return full AuthResponse structure
		return new AuthResponse(token, user.getId(), user.getRole());
	}
}
