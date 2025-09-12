const { ApolloServer } = require('apollo-server');
const { ApolloGateway, IntrospectAndCompose, RemoteGraphQLDataSource } = require('@apollo/gateway');

const SERVICE_JWT = process.env.SERVICE_JWT; // must be set before starting

// Custom DataSource
class AuthenticatedDataSource extends RemoteGraphQLDataSource {
  willSendRequest({ request, context }) {
    const query = request.body.query || "";

    if (query.includes("_service")) {
      console.log("👉 Federation introspection detected. Setting SERVICE_JWT");
      request.http.headers.set("Authorization", `Bearer ${SERVICE_JWT}`);
    } else if (context.token) {
      console.log("👉 Forwarding user token:", context.token);
      request.http.headers.set("Authorization", context.token);
    }
  }
}

const gateway = new ApolloGateway({
  supergraphSdl: new IntrospectAndCompose({
    subgraphs: [
      { name: 'book', url: 'http://localhost:8081/graphql' },
      { name: 'student', url: 'http://localhost:8080/graphql' },
    ],
  }),
  buildService({ url }) {
    return new AuthenticatedDataSource({ url });
  },
});

const server = new ApolloServer({
  gateway,
  subscriptions: false,
  introspection: true,
  playground: true,

  context: ({ req }) => {
    const authHeader = req?.headers?.authorization || '';
    return { token: authHeader };
  },
});

server.listen({ port: 4000 }).then(({ url }) => {
  console.log(`🚀 Apollo Gateway running at ${url}`);
});
