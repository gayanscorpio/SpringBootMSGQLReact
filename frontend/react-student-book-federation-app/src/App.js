// src/App.js
import React from 'react';
import StudentList from './StudentList';
import AddStudent from './AddStudent';
import EditStudent from './EditStudent'; // Pass studentId dynamically
import DeleteStudent from './DeleteStudent'; // Pass studentId dynamically
import BookList from './BookList';
import AddBook from './AddBook';

function App() {
  return (
    <div>
      <h1>Student and Book Management</h1>

      <AddStudent />
      <StudentList />
      <EditStudent studentId="1" />
      <DeleteStudent studentId="1" />

      <AddBook />
      <BookList />
    </div>
  );
}

export default App;
