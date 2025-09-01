// src/AddStudent.js
import React, { useState } from 'react';
import { gql, useMutation } from '@apollo/client';

const ADD_STUDENT = gql`
  mutation AddStudent($name: String!, $email: String!) {
    addStudent(name: $name, email: $email) {
      id
      name
      email
    }
  }
`;

function AddStudent() {
    const [name, setName] = useState('');
    const [email, setEmail] = useState('');
    const [addStudent, { data, loading, error }] = useMutation(ADD_STUDENT);

    const handleSubmit = (e) => {
        e.preventDefault();
        addStudent({ variables: { name, email } });
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
            {error && <p>Error: {error.message}</p>}
        </div>
    );
}

export default AddStudent;
