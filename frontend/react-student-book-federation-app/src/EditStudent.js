// src/EditStudent.js
import React, { useState } from 'react';
import { gql, useMutation } from '@apollo/client';

const UPDATE_STUDENT = gql`
  mutation UpdateStudent($id: ID!, $name: String!, $email: String!) {
    updateStudent(id: $id, name: $name, email: $email) {
      id
      name
      email
    }
  }
`;

function EditStudent({ studentId }) {
    const [name, setName] = useState('');
    const [email, setEmail] = useState('');
    const [updateStudent, { data, loading, error }] = useMutation(UPDATE_STUDENT);

    const handleSubmit = (e) => {
        e.preventDefault();
        updateStudent({ variables: { id: studentId, name, email } });
    };

    return (
        <div>
            <h2>Edit Student</h2>
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
                    {loading ? 'Updating...' : 'Update Student'}
                </button>
            </form>
            {data && <p>Updated: {data.updateStudent.name}</p>}
            {error && <p>Error: {error.message}</p>}
        </div>
    );
}

export default EditStudent;
