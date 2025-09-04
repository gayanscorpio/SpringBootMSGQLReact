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
  playground: true,       // Optional: enable GraphQL playground

  // Context function runs for each request
  context: ({ req }) => {
    const authHeader = req.headers.authorization || '';

    // Simple format check: must start with 'Bearer '
    if (!authHeader.startsWith('Bearer ')) {
      console.warn('No valid Authorization header found.');
    }

    // Forward header downstream via context
    return { token: authHeader };
  },

  // Modify requests sent to services
  buildService({ url }) {
    const { RemoteGraphQLDataSource } = require('@apollo/gateway');

    return new RemoteGraphQLDataSource({
      url,
      willSendRequest({ request, context }) {
        // Forward the token to the downstream service
        if (context.token) {
          request.http.headers.set('Authorization', context.token);
        }
      },
    });
  },

});

server.listen({ port: 4000 }).then(({ url }) => {
  console.log(`🚀 Apollo Gateway running at ${url}`);
});

