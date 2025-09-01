const { ApolloServer } = require('apollo-server');
const { ApolloGateway } = require('@apollo/gateway');

const gateway = new ApolloGateway({
  serviceList: [
    { name: 'book', url: 'http://localhost:8081/graphql' },
    { name: 'student', url: 'http://localhost:8080/graphql' },
  ]
});

const server = new ApolloServer({
  gateway,
  subscriptions: false, // Subscriptions are not yet supported with Gateway
  introspection: true,   // Optional: useful during development
  playground: true       // Optional: enable GraphQL playground
});

server.listen({ port: 4000 }).then(({ url }) => {
  console.log(`🚀 Apollo Gateway running at ${url}`);
});

