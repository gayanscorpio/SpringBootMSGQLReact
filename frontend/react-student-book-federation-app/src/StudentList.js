// src/StudentList.js
import React from 'react';
import { gql, useQuery } from '@apollo/client';

const GET_STUDENTS = gql`
  query GetStudents {
    allStudents {
      id
      name
      email
      borrowedBooksCount
    }
  }
`;

function StudentList() {
    const { loading, error, data } = useQuery(GET_STUDENTS);

    if (loading) return <p>Loading...</p>;
    if (error) return <p>Error: {error.message}</p>;

    return (
        <div>
            <h2>Students List</h2>
            <table>
                <thead>
                    <tr>
                        <th>Name</th>
                        <th>Email</th>
                        <th>Borrowed Books Count</th>
                    </tr>
                </thead>
                <tbody>
                    {data.allStudents.map(student => (
                        <tr key={student.id}>
                            <td>{student.name}</td>
                            <td>{student.email}</td>
                            <td>{student.borrowedBooksCount}</td>
                        </tr>
                    ))}
                </tbody>
            </table>
        </div>
    );
}

export default StudentList;
