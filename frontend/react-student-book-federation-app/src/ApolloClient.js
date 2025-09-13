// src/ApolloClient.js
import { ApolloClient, InMemoryCache, createHttpLink } from '@apollo/client';
import { setContext } from '@apollo/client/link/context';

// HTTP link to Apollo Gateway
const httpLink = createHttpLink({
    uri: 'http://localhost:4000/graphql',
});

// Middleware to add Authorization header
const authLink = setContext((_, { headers }) => {
    // Get token from localStorage (set after login)
    const token = localStorage.getItem('token');

    return {
        headers: {
            ...headers,
            authorization: token ? `Bearer ${token}` : '', // send token if exists
        },
    };
});

// Create Apollo Client with custom cache policies
const client = new ApolloClient({
    link: authLink.concat(httpLink), // chain auth link and http link
    cache: new InMemoryCache({
        typePolicies: {
            Student: {
                keyFields: ['id'], // normalize by ID
            },
            Book: {
                keyFields: ['id'], // normalize by ID
                fields: {
                    borrowedBy: {
                        // Merge function tells Apollo how to safely replace borrowedBy
                        merge(existing, incoming) {
                            return incoming; // Accept new value even if null
                        },
                    },
                },
            },
        }
    }),
});

export default client;
