// index.js
import { ApolloServer } from '@apollo/server';
import { startStandaloneServer } from '@apollo/server/standalone';
import { ApolloGateway, IntrospectAndCompose, RemoteGraphQLDataSource } from '@apollo/gateway';

const SERVICE_JWT = process.env.SERVICE_JWT; // must be set before starting

// ✅ Custom DataSource with Auth
class AuthenticatedDataSource extends RemoteGraphQLDataSource {
  willSendRequest({ request, context }) {
    const query = request.query || "";

    // Federation introspection (_service query)
    if (query.includes("_service") && SERVICE_JWT) {
      console.log("👉 Federation introspection detected, using SERVICE_JWT");
      request.http.headers.set("Authorization", `Bearer ${SERVICE_JWT}`);
    }
    // Forward user token
    else if (context.token) {
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
  introspection: true, // Playground removed, but introspection still possible
});

// 🚀 Start standalone server
const { url } = await startStandaloneServer(server, {
  listen: { port: 4000 },
  context: async ({ req }) => {
    const authHeader = req?.headers?.authorization || '';
    return { token: authHeader };
  },
});

console.log(`🚀 Apollo Gateway running at ${url}`);
