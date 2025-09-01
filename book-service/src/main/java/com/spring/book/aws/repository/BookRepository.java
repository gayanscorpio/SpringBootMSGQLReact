package com.spring.book.aws.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.spring.book.aws.model.Book;

public interface BookRepository extends JpaRepository<Book, String> {

}
