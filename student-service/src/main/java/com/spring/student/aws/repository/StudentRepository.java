package com.spring.student.aws.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.spring.student.aws.model.Student;

public interface StudentRepository extends JpaRepository<Student, String> {

	// No need to declare findById(String id) because it's already inherited from
	// JpaRepository
	// Student findById(String id);

}
