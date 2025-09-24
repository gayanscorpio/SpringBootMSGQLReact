// src/BookList.js
import React, { useEffect } from 'react';
import { gql, useQuery } from '@apollo/client';

// Query to fetch all books
const GET_BOOKS = gql`
  query GetBooks {
    allBooks {
      id
      title
      author
      availableCopies
    }
  }
`;

// Subscription for newly added books
const BOOK_ADDED = gql`
  subscription {
    bookAdded {
      id
      title
      author
      availableCopies
    }
  }
`;

function BookList() {
    const { data, loading, error, subscribeToMore } = useQuery(GET_BOOKS);

    useEffect(() => {
        console.log("[BookList] Setting up subscription...");
        // Subscribe to new books
        const unsubscribe = subscribeToMore({
            document: BOOK_ADDED,
            onError: (err) => console.error("[BookList] Subscription error:", err),

            updateQuery: (prev, { subscriptionData }) => {
                console.log("[BookList] Raw subscriptionData:", subscriptionData);
                const newBook = subscriptionData?.data?.bookAdded;

                if (!newBook) {
                    console.warn("[BookList] Subscription payload is empty, ignoring.");
                    return prev;
                }

                // Log the full object for debugging
                console.log("[BookList] 📢 Received new book from subscription:\n", JSON.stringify(newBook, null, 2));

                // Prevent duplicates
                if (prev.allBooks.some((b) => b.id === newBook.id)) {
                    return prev;
                }

                // Return updated list
                return {
                    ...prev,
                    allBooks: [newBook, ...prev.allBooks],
                };
            },
        });

        return () => {
            console.log("[BookList] Unsubscribing from BOOK_ADDED...");
            unsubscribe();
        };
    }, [subscribeToMore]);

    if (loading) return <p>Loading books…</p>;
    if (error) return <p>Error: {error.message}</p>;

    return (
        <div>
            <h2>Books List</h2>
            <table>
                <thead>
                    <tr>
                        <th>Title</th>
                        <th>Author</th>
                        <th>Available Copies</th>
                    </tr>
                </thead>
                <tbody>
                    {data?.allBooks?.map((book) => (
                        <tr key={book.id}>
                            <td>{book.title}</td>
                            <td>{book.author}</td>
                            <td>{book.availableCopies}</td>
                        </tr>
                    ))}
                </tbody>
            </table>
        </div>
    );
}

export default BookList;
