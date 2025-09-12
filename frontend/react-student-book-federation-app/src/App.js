// src/App.js
import React, { useState, useEffect } from 'react';
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
  const [role, setRole] = useState(localStorage.getItem('role')); // 👈 store role

  // Run once on mount to sync state with localStorage
  useEffect(() => {
    setRole(localStorage.getItem('role'));
  }, []);

  console.log('<<<<<<<<< isLoggedIn:', isLoggedIn, 'role:', role);
  // Page loads → Root renders <Login /> if no token.
  if (!isLoggedIn) {
    return (
      <Login
        onLoginSuccess={(userRole) => {
          setIsLoggedIn(true);
          setRole(userRole);
        }}
      />
    );
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
              localStorage.removeItem('role');
              setIsLoggedIn(false);
              setRole(null);
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
                {/* 👇 Only Admin sees AddBook */}
                {role === 'Admin' && <AddBook />}
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
