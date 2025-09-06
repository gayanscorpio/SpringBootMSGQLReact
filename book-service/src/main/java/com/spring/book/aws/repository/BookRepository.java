package com.spring.book.aws.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.spring.book.aws.model.Book;

public interface BookRepository extends JpaRepository<Book, String> {

	@Query("SELECT COUNT(b) FROM Book b WHERE :studentId IN elements(b.borrowedStudentIds)")
	Integer countByBorrowedStudentId(@Param("studentId") String studentId);

	@Query("SELECT b FROM Book b WHERE :studentId IN elements(b.borrowedStudentIds)")
	List<Book> findByBorrowedStudentId(@Param("studentId") String studentId);

}
