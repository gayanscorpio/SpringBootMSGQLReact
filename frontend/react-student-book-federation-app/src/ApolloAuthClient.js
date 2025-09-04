// src/ApolloAuthClient.js
import { ApolloClient, InMemoryCache, createHttpLink } from '@apollo/client';

const authClient = new ApolloClient({
    link: createHttpLink({
        uri: 'http://localhost:8082/graphql', // Auth service
    }),
    cache: new InMemoryCache(),
});

export default authClient;
