// src/DeleteStudent.js
import React from 'react';
import { gql, useMutation } from '@apollo/client';

const DELETE_STUDENT = gql`
  mutation DeleteStudent($id: ID!) {
    deleteStudent(id: $id)
  }
`;

function DeleteStudent({ studentId }) {
    const [deleteStudent, { loading, error }] = useMutation(DELETE_STUDENT);

    const handleDelete = () => {
        deleteStudent({ variables: { id: studentId } });
    };

    return (
        <div>
            <button onClick={handleDelete} disabled={loading}>
                {loading ? 'Deleting...' : 'Delete Student'}
            </button>
            {error && <p>Error: {error.message}</p>}
        </div>
    );
}

export default DeleteStudent;
