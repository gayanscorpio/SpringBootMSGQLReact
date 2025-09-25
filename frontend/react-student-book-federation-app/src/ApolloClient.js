// src/ApolloClient.js
import { ApolloClient, InMemoryCache, createHttpLink, split, gql } from '@apollo/client';
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
    const token = localStorage.getItem('token');
    return {
        headers: {
            ...headers,
            authorization: token ? `Bearer ${token}` : '',
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
        lazy: false,
        reconnect: true,
        onConnect: (ctx) =>
            console.log('WS Client connected:', ctx.connectionParams),
    })
);

// Split link: Queries/Mutations → HTTP, Subscriptions → WS
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

// Apollo Client with typePolicies
const client = new ApolloClient({
    link: splitLink,
    cache: new InMemoryCache({
        typePolicies: {
            Query: {
                fields: {
                    allBooks: {
                        keyArgs: false, // treat allBooks as one list
                        merge(existing = [], incoming, { readField }) {
                            const merged = [...existing];
                            for (const book of incoming) {
                                const id = readField('id', book);
                                if (!merged.some((b) => readField('id', b) === id)) {
                                    merged.push(book);
                                }
                            }
                            return merged;
                        },
                    },
                },
            },
            Student: {
                keyFields: ['id'],
            },
            Book: {
                keyFields: ['id'],
                fields: {
                    borrowedBy: {
                        merge(_, incoming) {
                            return incoming; // always accept new value
                        },
                    },
                },
            },
        },
    }),
});

export default client;
