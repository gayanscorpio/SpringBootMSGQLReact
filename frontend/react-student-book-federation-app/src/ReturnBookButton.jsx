import React from "react";
import { gql, useMutation } from "@apollo/client";

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

function ReturnBookButton({ bookId, studentId }) {
    const [returnBook] = useMutation(RETURN_BOOK, {
        update(cache, { data: { returnBook } }) {
            cache.writeFragment({
                id: `Book:${returnBook.id}`,
                fragment: gql`
          fragment ReturnedBook on Book {
            id
            title
            author
            availableCopies
            borrowedBy {
              id
              name
            }
          }
        `,
                data: returnBook,
            });

            cache.modify({
                id: `Student:${studentId}`,
                fields: {
                    borrowedBooksCount(existingCount = 0) {
                        return Math.max(existingCount - 1, 0);
                    },
                    borrowedBooks(existingBooks = []) {
                        return existingBooks.filter(
                            (bookRef) => bookRef.__ref !== `Book:${returnBook.id}`
                        );
                    },
                },
            });
        },
    });

    const handleReturn = async () => {
        await returnBook({ variables: { bookId, studentId } });
    };

    return <button onClick={handleReturn}>Return</button>;
}

export default ReturnBookButton;
