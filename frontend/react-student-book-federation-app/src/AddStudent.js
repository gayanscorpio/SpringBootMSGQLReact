// src/AddStudent.js
import React, { useState } from 'react';
import { gql, useMutation } from '@apollo/client';
import { GET_STUDENTS } from './StudentList'; // Import the query to update cache

const ADD_STUDENT = gql`
  mutation AddStudent($name: String!, $email: String!) {
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

function AddStudent() {
    const [name, setName] = useState('');
    const [email, setEmail] = useState('');

    const [addStudent, { data, loading, error }] = useMutation(ADD_STUDENT, {
        update(cache, { data: { addStudent } }) {
            try {
                // Read existing students from cache
                const existingData = cache.readQuery({ query: GET_STUDENTS });

                if (existingData) {
                    // Write back with the new student added
                    cache.writeQuery({
                        query: GET_STUDENTS,
                        data: {
                            allStudents: [...existingData.allStudents, addStudent],
                        },
                    });
                }
            } catch (e) {
                console.warn("Cache update skipped:", e);
            }
        },
    });

    const handleSubmit = (e) => {
        e.preventDefault();

        if (!name || !email) return alert("Please enter name and email");
        addStudent({ variables: { name, email } });

        // Clear form after adding
        setName('');
        setEmail('');
    };

    return (
        <div>
            <h2>Add New Student</h2>
            <form onSubmit={handleSubmit}>
                <input
                    type="text"
                    placeholder="Student Name"
                    value={name}
                    onChange={(e) => setName(e.target.value)}
                />
                <input
                    type="email"
                    placeholder="Student Email"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                />
                <button type="submit" disabled={loading}>
                    {loading ? 'Adding...' : 'Add Student'}
                </button>
            </form>
            {data && <p>Added: {data.addStudent.name}</p>}
            {error && <p style={{ color: 'red' }}>Error: {error.message}</p>}
        </div>
    );
}

export default AddStudent;
