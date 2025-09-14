// src/AddBook.js
import React, { useState } from 'react';
import { gql, useMutation } from '@apollo/client';

const ADD_BOOK = gql`
  mutation AddBook($title: String!, $author: String!, $totalCopies: Int!) {
    addBook(title: $title, author: $author, totalCopies: $totalCopies) {
      id
      title
      author
      totalCopies
      availableCopies
    }
  }
`;

// Query used in BookList
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

function AddBook() {
    const [title, setTitle] = useState('');
    const [author, setAuthor] = useState('');
    const [totalCopies, setTotalCopies] = useState(1);

    const [addBook, { data, loading, error }] = useMutation(ADD_BOOK, {
        update(cache, { data: { addBook } }) {
            // Read the current books from the cache
            const existingBooks = cache.readQuery({ query: GET_BOOKS });

            // Write the new book list back to the cache
            if (existingBooks) {
                cache.writeQuery({
                    query: GET_BOOKS,
                    data: {
                        allBooks: [...existingBooks.allBooks, addBook],
                    },
                });
            }
        },
    });

    const handleSubmit = (e) => {
        e.preventDefault();
        if (!title || !author || totalCopies < 1) return;

        addBook({ variables: { title, author, totalCopies } });

        // Reset form
        setTitle('');
        setAuthor('');
        setTotalCopies(1);
    };

    return (
        <div>
            <h2>Add New Book</h2>
            <form onSubmit={handleSubmit}>
                <input
                    type="text"
                    placeholder="Book Title"
                    value={title}
                    onChange={(e) => setTitle(e.target.value)}
                />
                <input
                    type="text"
                    placeholder="Author"
                    value={author}
                    onChange={(e) => setAuthor(e.target.value)}
                />
                <input
                    type="number"
                    placeholder="Total Copies"
                    value={totalCopies}
                    onChange={(e) => setTotalCopies(parseInt(e.target.value))}
                />
                <button type="submit" disabled={loading}>
                    {loading ? 'Adding...' : 'Add Book'}
                </button>
            </form>
            {data && <p>Added: {data.addBook.title}</p>}
            {error && <p style={{ color: 'red' }}>Error: {error.message}</p>}
        </div>
    );
}

export default AddBook;
