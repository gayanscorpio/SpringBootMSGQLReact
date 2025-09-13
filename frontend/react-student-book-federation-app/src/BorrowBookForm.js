import React, { useState } from "react";
import { gql, useQuery, useMutation } from "@apollo/client";
import { GET_STUDENTS } from './StudentList'; // import your query for cache update

// Query to get student and their borrowed books
export const GET_AVAILABLE_BOOKS = gql`
  query GetBooks {
    allBooks {
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

// Mutation: borrow a book
const BORROW_BOOK = gql`
  mutation BorrowBook($bookId: ID!, $studentId: ID!) {
    borrowBook(bookId: $bookId, studentId: $studentId) {
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

function BorrowBookForm({ studentId, onClose }) {
    const { loading, error, data } = useQuery(GET_AVAILABLE_BOOKS);
    const [selectedBookId, setSelectedBookId] = useState(null);

    const [borrowBook] = useMutation(BORROW_BOOK, {
        update(cache, { data: { borrowBook } }) {
            if (!borrowBook) return;

            // ✅ Update Book in cache
            try {
                cache.writeFragment({
                    id: `Book:${borrowBook.id}`,
                    fragment: gql`
            fragment BorrowedBook on Book {
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
                    data: borrowBook,
                });
            } catch (e) {
                console.warn("Book cache update skipped:", e);
            }

            // ✅ Update the specific student only
            try {
                cache.modify({
                    id: `Student:${studentId}`,
                    fields: {
                        borrowedBooksCount(existingCount = 0) {
                            return existingCount + 1;
                        },
                        borrowedBooks(existingBooks = []) {
                            return [...existingBooks, { __ref: `Book:${borrowBook.id}` }];
                        },
                    },
                });
            } catch (e) {
                console.warn("Student cache update skipped:", e);
            }

            // 3️⃣ Update GET_STUDENTS so StudentList table refreshes instantly
            try {
                const existingData = cache.readQuery({ query: GET_STUDENTS });
                if (existingData) {
                    cache.writeQuery({
                        query: GET_STUDENTS,
                        data: {
                            allStudents: existingData.allStudents.map((s) =>
                                s.id === studentId
                                    ? {
                                        ...s,
                                        borrowedBooksCount: (s.borrowedBooksCount || 0) + 1,
                                        borrowedBooks: [
                                            ...s.borrowedBooks,
                                            { __typename: "Book", id: borrowBook.id, title: borrowBook.title, author: borrowBook.author, availableCopies: borrowBook.availableCopies, borrowedBy: borrowBook.borrowedBy }
                                        ]
                                    }
                                    : s
                            ),
                        },
                    });
                }
            } catch (e) {
                console.warn("GET_STUDENTS cache update skipped:", e);
            }
        }
    });


    if (loading) return <p>Loading books...</p>;
    if (error) return <p>Error: {error.message}</p>;

    const handleBorrow = async () => {
        if (!selectedBookId) return alert("Select a book to borrow!");
        try {
            await borrowBook({ variables: { bookId: selectedBookId, studentId } });
            onClose();
        } catch (err) {
            console.error("Borrow book failed:", err);
            alert("Failed to borrow book. Please try again.");
        }
    };

    // Filter available books
    const availableBooks = (data?.allBooks || []).filter(
        (book) =>
            book &&
            book.availableCopies != null &&
            book.availableCopies > 0 &&
            (!book.borrowedBy || !book.borrowedBy.some((s) => s.id === studentId))
    );

    return (
        <div>
            <select
                value={selectedBookId || ""}
                onChange={(e) => setSelectedBookId(e.target.value)}
            >
                <option value="">Select a book</option>
                {availableBooks.map((book) => (
                    <option key={book.id} value={book.id}>
                        {book.title} by {book.author} ({book.availableCopies} available)
                    </option>
                ))}
            </select>
            <button onClick={handleBorrow} disabled={!selectedBookId}>
                Borrow
            </button>
            <button onClick={onClose}>Cancel</button>
        </div>
    );
}

export default BorrowBookForm;
