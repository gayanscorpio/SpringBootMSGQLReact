// src/Login.js
import React, { useState } from 'react';
import { gql, useMutation } from '@apollo/client';
import authClient from './ApolloAuthClient';

const LOGIN_MUTATION = gql`
  mutation login($username: String!, $password: String!) {
    login(username: $username, password: $password) {
      token
      userId
    }
  }
`;

function Login({ onLoginSuccess }) {
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [login, { loading, error }] = useMutation(LOGIN_MUTATION, {
        client: authClient
    });

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            const { data } = await login({ variables: { username, password } });
            localStorage.setItem('token', data.login.token); // store token
            const userId = data.login.userId; // ✅ matches schema
            console.log('User ID:', userId);
            onLoginSuccess(); // notify parent that login succeeded
        } catch (err) {
            console.error(err);
        }
    };

    return (
        <div>
            <h2>Login</h2>
            <form onSubmit={handleSubmit}>
                <input
                    type="text"
                    placeholder="Username"
                    value={username}
                    onChange={(e) => setUsername(e.target.value)}
                    required
                />
                <input
                    type="password"
                    placeholder="Password"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    required
                />
                <button type="submit" disabled={loading}>Login</button>
            </form>
            {error && <p style={{ color: 'red' }}>Login failed</p>}
        </div>
    );
}

export default Login;
