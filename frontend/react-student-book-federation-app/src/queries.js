// src/queries.js
import { gql } from '@apollo/client';

//borrowedBooksCount, borrowedBooks: resolved by Book service through Federated server
export const GET_STUDENTS = gql`
  query GetStudents {
    allStudents {
      id
      name
      email
      borrowedBooksCount
      borrowedBooks {
        id
        title
        author
        availableCopies
      }
    }
  }
`;

export const GET_BOOKS = gql`
  query GetBooks {
    allBooks {
      id
      title
      author
      availableCopies
    }
  }
`;

export const ADD_STUDENT = gql`
  mutation AddStudent($name: String!) {
    addStudent(name: $name, email: $email) {
      id
      name
      email
      borrowedBooksCount
      borrowedBooks {
        id
        title
        author
        availableCopies
      }
    }
  }
`;

export const DELETE_STUDENT = gql`
  mutation DeleteStudent($id: ID!) {
    deleteStudent(id: $id)
  }
`;

export const BORROW_BOOK = gql`
  mutation BorrowBook($studentId: ID!, $bookId: ID!) {
    borrowBook(studentId: $studentId, bookId: $bookId) {
      id
      borrowedBooks {
        id
        title
      }
    }
  }
`;
