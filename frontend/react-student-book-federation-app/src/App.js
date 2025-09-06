// src/App.js
import React, { useState } from 'react';
import { BrowserRouter as Router, Routes, Route, Link, Navigate } from 'react-router-dom';
import StudentList from './StudentList';
import AddStudent from './AddStudent';
import EditStudent from './EditStudent'; // Pass studentId dynamically
import DeleteStudent from './DeleteStudent'; // Pass studentId dynamically
import BookList from './BookList';
import AddBook from './AddBook';
import Login from './Login';

function App() {
  const [isLoggedIn, setIsLoggedIn] = useState(!!localStorage.getItem('token'));
  const [selectedStudentId, setSelectedStudentId] = useState(null);

  console.log('<<<<<<<<< isLoggedIn:', isLoggedIn)
  // Page loads → Root renders <Login /> if no token.
  if (!isLoggedIn) {
    return <Login onLoginSuccess={() => setIsLoggedIn(true)} />;
  }

  return (
    <Router>
      <div>
        <h1>Student and Book Management</h1>

        <nav>
          <Link to="/students">Students</Link> |{" "}
          <Link to="/books">Books</Link> |{" "}
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
        </nav>
        <hr />

        <Routes>
          <Route path="/" element={<Navigate to="/students" />} />

          {/* Students Page */}
          <Route
            path="/students"
            element={
              <div>
                <AddStudent />
                <StudentList
                  onEdit={(id) => setSelectedStudentId(id)}
                  onDelete={(id) => setSelectedStudentId(id)}
                />
                {selectedStudentId && (
                  <div>
                    <h3>Actions for Student {selectedStudentId}</h3>
                    <EditStudent studentId={selectedStudentId} />
                    <DeleteStudent studentId={selectedStudentId} />
                  </div>
                )}
              </div>
            }
          />

          {/* Books Page */}
          <Route
            path="/books"
            element={
              <div>
                <AddBook />
                <BookList />
              </div>
            }
          />
        </Routes>
      </div>
    </Router>
  );
}

export default App;
