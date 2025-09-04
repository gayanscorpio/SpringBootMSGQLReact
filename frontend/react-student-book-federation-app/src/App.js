// src/App.js
import React, { useState } from 'react';
import StudentList from './StudentList';
import AddStudent from './AddStudent';
import EditStudent from './EditStudent'; // Pass studentId dynamically
import DeleteStudent from './DeleteStudent'; // Pass studentId dynamically
import BookList from './BookList';
import AddBook from './AddBook';
import Login from './Login';

function App() {
  const [isLoggedIn, setIsLoggedIn] = useState(
    !!localStorage.getItem('token')
  );

  console.log('<<<<<<<<< isLoggedIn:', isLoggedIn)
  // Page loads → Root renders <Login /> if no token.
  if (!isLoggedIn) {
    return <Login onLoginSuccess={() => setIsLoggedIn(true)} />;
  }

  return (
    <div>
      <h1>Student and Book Management</h1>


      {/* Logout link */}
      <a
        href="#"
        onClick={() => {
          localStorage.removeItem('token');
          setIsLoggedIn(false);
        }}
      >
        Logout
      </a>

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
