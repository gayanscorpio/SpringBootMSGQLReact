package com.spring.auth.aws.model;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user_pin") // maps to table user_pin
public class UserPin {
	@Id
	private String userId; // FK to AppUser.id

	private String pinHash; // hashed PIN (BCrypt)

	private int failedAttempts; // count of incorrect PIN tries

	private LocalDateTime lockedUntil; // temporary lockout if max attempts exceeded

	private LocalDateTime createdAt = LocalDateTime.now(); // timestamp of creation
}
