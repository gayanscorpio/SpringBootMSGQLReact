package com.spring.student.aws.model;

import lombok.Data;

@Data
public class Book {
	private String id, title, author, borrowedStudentId;

}
