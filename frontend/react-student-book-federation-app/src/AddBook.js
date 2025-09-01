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
    }
  }
`;

function AddBook() {
    const [title, setTitle] = useState('');
    const [author, setAuthor] = useState('');
    const [totalCopies, setTotalCopies] = useState(1);
    const [addBook, { data, loading, error }] = useMutation(ADD_BOOK);

    const handleSubmit = (e) => {
        e.preventDefault();
        addBook({ variables: { title, author, totalCopies } });
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
            {error && <p>Error: {error.message}</p>}
        </div>
    );
}

export default AddBook;
