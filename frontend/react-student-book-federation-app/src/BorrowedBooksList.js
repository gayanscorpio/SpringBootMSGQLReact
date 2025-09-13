// src/BorrowedBooksList.js
import React from 'react';
import { gql, useQuery, useMutation } from '@apollo/client';

// GraphQL query: fetch student and their borrowed books
const GET_BORROWED_BOOKS = gql`
  query GetBorrowedBooks($id: ID!) {
    studentById(id: $id) {
      id
      name
      borrowedBooks {
        id
        title
        author
        availableCopies
        borrowedBy {
          id
          name
        }
      }
    }
  }
`;

// GraphQL mutation: return a book
const RETURN_BOOK = gql`
  mutation ReturnBook($bookId: ID!, $studentId: ID!) {
    returnBook(bookId: $bookId, studentId: $studentId) {
      id
      title
      author
      availableCopies
      borrowedBy {
        id
        name
      }
    }
  }
`;

function BorrowedBooksList({ studentId }) {
  const { loading, error, data } = useQuery(GET_BORROWED_BOOKS, {
    variables: { id: studentId },
  });

  const [returnBook] = useMutation(RETURN_BOOK, {
    update(cache, { data: { returnBook } }) {
      if (!returnBook) return;

      // Remove the returned book from student.borrowedBooks
      cache.modify({
        id: cache.identify({ __typename: "Student", id: studentId }),
        fields: {
          borrowedBooks(existingBooks = [], { readField }) {
            return existingBooks.filter(
              (bookRef) => readField("id", bookRef) !== returnBook.id
            );
          },
          borrowedBooksCount(existingCount = 1) {
            return Math.max(existingCount - 1, 0);
          },
        },
      });

      // 2️⃣ Update Book object fields in cache safely
      cache.writeFragment({
        id: cache.identify({ __typename: 'Book', id: returnBook.id }),
        fragment: gql`
          fragment ReturnedBook on Book {
            id
            availableCopies
            borrowedBy {
              id
              name
            }
          }
        `,
        data: {
          ...returnBook,
          borrowedBy: returnBook.borrowedBy || [], // prevent null
        },
      });
    },
  });

  if (loading) return <p>Loading borrowed books...</p>;
  if (error) return <p>Error: {error.message}</p>;

  const books = data?.studentById?.borrowedBooks || [];
  console.log('books <<<<<<< : ', books)

  if (books.length === 0) return <p>No borrowed books.</p>;

  const handleReturn = async (bookId) => {
    try {
      await returnBook({ variables: { bookId, studentId } });
      alert("Book returned successfully!");
    } catch (err) {
      console.error("Return book failed:", err);
      alert("Failed to return book. Please try again.");
    }
  };

  return (
    <div>
      <h4>{data.studentById.name}'s Borrowed Books:</h4>
      <ul>
        {books.map((book) => (
          <li key={book.id}>
            <strong>{book.title}</strong> by {book.author} (Available:{" "}
            {book.availableCopies})
            <br />
            Borrowed by:{" "}
            {book.borrowedBy && book.borrowedBy.length > 0
              ? book.borrowedBy
                .map((student) =>
                  student.name ? student.name : `ID: ${student.id}`
                )
                .join(", ")
              : "No one"}
            <br />
            <button onClick={() => handleReturn(book.id)}>Return</button>
          </li>
        ))}
      </ul>
    </div>
  );
}

export default BorrowedBooksList;
