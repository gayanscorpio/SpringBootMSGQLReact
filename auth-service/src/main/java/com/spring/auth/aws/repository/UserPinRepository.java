package com.spring.auth.aws.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.spring.auth.aws.model.UserPin;

public interface UserPinRepository extends JpaRepository<UserPin, String> {
	// userId is the primary key
}
