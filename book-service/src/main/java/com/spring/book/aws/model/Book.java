package com.spring.book.aws.model;

import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Table(name = "book")
@AllArgsConstructor
@NoArgsConstructor
public class Book {

	@Id
	private String id = UUID.randomUUID().toString();
	private String title;
	private String author;
	private int totalCopies;
	private int availableCopies;
	private String borrowedStudentId;

}
