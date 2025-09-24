// src/BookList.js
import React, { useEffect, useState } from "react";
import { useSubscription, gql } from "@apollo/client";

// GraphQL subscription
const BOOK_ADDED_SUBSCRIPTION = gql`
  subscription {
    bookAdded {
      id
      title
      author
      totalCopies
      availableCopies
    }
  }
`;

function BookList() {
    const [books, setBooks] = useState([]);

    // Apollo subscription hook (no onData now)
    const { data, loading, error } = useSubscription(BOOK_ADDED_SUBSCRIPTION);

    console.log("<<<<<<<<<<<<<<<<<<<< data >>>>>>>>>>>>>>>>>>>>", data);

    // Handle new data when it arrives
    useEffect(() => {
        if (data?.bookAdded) {
            console.log("Raw subscription object:", data);
            const newBook = data.bookAdded;

            setBooks((prevBooks) => {
                if (prevBooks.some((b) => b?.id === newBook.id)) return prevBooks; // avoid duplicates
                return [...prevBooks, newBook];
            });
        }
    }, [data]);

    // Log errors if subscription fails
    useEffect(() => {
        if (error) console.error("Subscription error:", error);
    }, [error]);

    if (loading && books.length === 0) return <p>Loading books...</p>;

    return (
        <div>
            <h2>Live Book List</h2>
            <ul>
                {books && books.length > 0 ? (
                    books
                        .filter((book) => book && book.title) // prevent null crash
                        .map((book) => (
                            <li key={book.id}>
                                <strong>{book.title}</strong> by {book.author} —{" "}
                                {book.availableCopies}/{book.totalCopies} copies available
                            </li>
                        ))
                ) : (
                    <li>No books yet</li>
                )}
            </ul>
        </div>
    );
}

export default BookList;
