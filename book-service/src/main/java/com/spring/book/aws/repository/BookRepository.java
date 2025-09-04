package com.spring.book.aws.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.spring.book.aws.model.Book;

public interface BookRepository extends JpaRepository<Book, String> {
	int countByBorrowedStudentId(String studentId);

	List<Book> findByBorrowedStudentId(String studentId);

}
