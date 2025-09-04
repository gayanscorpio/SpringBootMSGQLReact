// src/index.js
import React, { useState } from 'react';
import { createRoot } from 'react-dom/client';
import { ApolloProvider } from '@apollo/client';
import App from './App';
import Login from './Login';
import client from './ApolloClient';

const container = document.getElementById('root');
const root = createRoot(container);

function Root() {
  const [isAuthenticated, setIsAuthenticated] = useState(
    !!localStorage.getItem('token') // check existing token
  );

  return (
    <ApolloProvider client={client}>
      {isAuthenticated ? (
        <App />
      ) : (
        <Login onLoginSuccess={() => setIsAuthenticated(true)} />
      )}
    </ApolloProvider>
  );
}

root.render(<Root />);
