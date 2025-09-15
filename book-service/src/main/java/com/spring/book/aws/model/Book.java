package com.spring.book.aws.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
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
	@ElementCollection(fetch = FetchType.EAGER) // load immediately to avoid lazy init errors: force eager loading
	private List<String> borrowedStudentIds = new ArrayList<>();

	// 👇 This enables optimistic locking
	@Version
	private Long version; // Optimistic locking

}
