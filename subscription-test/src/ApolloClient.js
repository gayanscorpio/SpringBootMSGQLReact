// src/ApolloClient.js
import { ApolloClient, InMemoryCache, createHttpLink, split } from '@apollo/client';
import { setContext } from '@apollo/client/link/context';
import { GraphQLWsLink } from '@apollo/client/link/subscriptions';
import { createClient } from 'graphql-ws';
import { getMainDefinition } from '@apollo/client/utilities';

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

// WebSocket link → Subscription server
const wsLink = new GraphQLWsLink(
    createClient({
        url: 'ws://localhost:5001/graphql',
        connectionParams: () => {
            const token = localStorage.getItem('token');
            return {
                authorization: token ? `Bearer ${token}` : '',
            };
        },
        lazy: false, // makes sure the client connects immediately and stays connected
        reconnect: true,  // automatically reconnect if disconnected
        onConnect: (ctx) => console.log("WS Client connected:", ctx.connectionParams)

    })
);

// Split link: Queries/Mutations use HTTP, Subscriptions use WS
const splitLink = split(
    ({ query }) => {
        const definition = getMainDefinition(query);
        return (
            definition.kind === 'OperationDefinition' &&
            definition.operation === 'subscription'
        );
    },
    wsLink,
    authLink.concat(httpLink)
);

// Create Apollo Client with custom cache policies
const client = new ApolloClient({
    link: splitLink, // chain auth link and http link, and ws link
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
