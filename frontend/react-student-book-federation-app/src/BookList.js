// src/BookList.js
import React from 'react';
import { gql, useQuery } from '@apollo/client'; //apollo client 

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

function BookList() {
    const { loading, error, data } = useQuery(GET_BOOKS);

    if (loading) return <p>Loading...</p>;
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
                    {data.allBooks.map(book => (
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
