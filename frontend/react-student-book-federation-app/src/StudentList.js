// src/StudentList.js
import React, { useState } from 'react';
import { gql, useQuery, useMutation } from '@apollo/client';
import BorrowedBooksList from './BorrowedBooksList'; // New component to show books
import BorrowBookForm from './BorrowBookForm'; // New component to select & borrow book

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

// Mutation: return book
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

function StudentList({ onEdit, onDelete }) {
    const { loading, error, data } = useQuery(GET_STUDENTS);
    const [selectedStudentId, setSelectedStudentId] = useState(null);
    const [borrowForStudentId, setBorrowForStudentId] = useState(null); // For borrow button

    const [returnBook] = useMutation(RETURN_BOOK, {
        update(cache, { data: { returnBook } }) {
            try {
                // Update Book in cache
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

                // Update Student's borrowedBooksCount and borrowedBooks
                const studentId = returnBook.borrowedBy?.id || null;
                if (studentId) {
                    cache.modify({
                        id: `Student:${studentId}`,
                        fields: {
                            borrowedBooksCount(existingCount = 1) {
                                return Math.max(existingCount - 1, 0);
                            },
                            borrowedBooks(existingBooks = [], { readField }) {
                                return existingBooks.filter(
                                    (b) => readField("id", b) !== returnBook.id
                                );
                            },
                        },
                    });
                }
            } catch (e) {
                console.warn("Cache update skipped:", e);
            }
        },
    });

    if (loading) return <p>Loading...</p>;
    if (error) return <p>Error: {error.message}</p>;

    const handleReturn = async (bookId, studentId) => {
        try {
            await returnBook({ variables: { bookId, studentId } });
            alert("Book returned successfully!");
        } catch (err) {
            console.error("Return book failed:", err);
            alert("Failed to return book. Please try again.");
        }
    };

    const role = localStorage.getItem("role"); // ✅ check logged-in user role

    return (
        <div>
            <h2>Students List</h2>
            <table border="1" cellPadding="6">
                <thead>
                    <tr>
                        <th>Name</th>
                        <th>Email</th>
                        <th>Borrowed Books</th>
                        <th>Actions</th>
                    </tr>
                </thead>
                <tbody>
                    {data.allStudents.map(student => (
                        <tr key={student.id}>
                            <td>{student.name}</td>
                            <td>{student.email}</td>
                            <td> {student.borrowedBooksCount > 0 ? (
                                <a
                                    href="#"
                                    onClick={(e) => {
                                        e.preventDefault();
                                        setSelectedStudentId(student.id);
                                    }}
                                >
                                    {student.borrowedBooksCount} borrowed
                                </a>
                            ) : (
                                "0"
                            )}</td>

                            <td>
                                {/* ✅ Show Edit/Delete only if Admin */}
                                {role === "Admin" && (
                                    <>
                                        <button onClick={() => onEdit(student.id)}>Edit</button>
                                        <button onClick={() => onDelete(student.id)}>Delete</button>
                                    </>
                                )}
                                {/* ✅ Borrow allowed for both Admin and Student */}
                                <button onClick={() => setBorrowForStudentId(student.id)}>
                                    Borrow Book
                                </button>
                            </td>
                        </tr>
                    ))}
                </tbody>
            </table>

            {/* Show borrowed books below table for selected student */}
            {selectedStudentId && (
                <div style={{ marginTop: "20px" }}>
                    <h3>Borrowed Books</h3>
                    <BorrowedBooksList
                        studentId={selectedStudentId}
                        borrowedBooks={
                            data.allStudents.find((s) => s.id === selectedStudentId)
                                ?.borrowedBooks
                        }
                        onReturn={handleReturn}
                    />
                </div>
            )}

            {/* Show Borrow Book Form */}
            {borrowForStudentId && (
                <div style={{ marginTop: "20px" }}>
                    <h3>Borrow Book for Student</h3>
                    <BorrowBookForm
                        studentId={borrowForStudentId}
                        onClose={() => setBorrowForStudentId(null)}
                    />
                </div>
            )}
        </div>
    );
}

export default StudentList;
