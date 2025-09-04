package com.spring.student.aws.model;

import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Table(name = "student")
@AllArgsConstructor
@NoArgsConstructor
public class Student {

	@Id
	private String id = UUID.randomUUID().toString();

	private String name;
	private String email;
}
