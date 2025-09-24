// subscription-server/index.js
import { ApolloServer } from '@apollo/server';
import { makeExecutableSchema } from '@graphql-tools/schema';
import { createServer } from 'http';
import { WebSocketServer } from 'ws';
import { useServer } from 'graphql-ws/lib/use/ws';
import { RedisPubSub } from 'graphql-redis-subscriptions';
import Redis from 'ioredis';

// --------------------
// Config
// --------------------
const BOOK_ADDED = 'BOOK_ADDED';

// Redis clients for publisher/subscriber
const subscriber = new Redis({ host: '127.0.0.1', port: 6379 });
const publisher = new Redis({ host: '127.0.0.1', port: 6379 });

// GraphQL PubSub (wrap Redis)
const pubsub = new RedisPubSub({ subscriber, publisher });

// --------------------
// GraphQL Schema
// --------------------
const typeDefs = `#graphql
  type Book {
    id: ID!
    title: String!
    author: String!
    totalCopies: Int!
    availableCopies: Int!
  }

  type Subscription {
    bookAdded: Book
  }

  type Query {
    _dummy: Boolean
  }
`;

// --------------------
// Resolvers
// --------------------
const resolvers = {
  Query: { _dummy: () => true },
  Subscription: {
    bookAdded: {
      subscribe: () => pubsub.asyncIterator([BOOK_ADDED]),
    },
  },
};

// --------------------
// Start server
// --------------------
async function startServer() {
  try {
    // --------------------
    // Subscribe to Redis channel
    // --------------------
    const count = await subscriber.subscribe(BOOK_ADDED);
    console.log(`✅ Subscribed to Redis channel ${BOOK_ADDED} (total ${count} subscriptions)`);

    // Log every raw Redis message
    subscriber.on('message', (channel, message) => {
      console.log('📩 Redis raw message on channel', channel, ':', message);

      try {
        const book = JSON.parse(message); // parse raw Book JSON
        console.log('📩 book', channel, ':', book);


        // Validate all required fields
        if (!book?.id || !book?.title || !book?.author) {
          console.warn('⚠️ Skipping invalid book payload:', book);
          return; // do not publish
        }

        // Wrap for GraphQL subscription clients
        // Always forward valid books
        pubsub.publish(BOOK_ADDED, { bookAdded: book });
        console.log('✅ Forwarded to GraphQL subscribers:', book);
      } catch (err) {
        console.error('❌ Failed to parse Redis message:', err, message);
      }
    });

    // --------------------
    // Apollo Server + Schema
    // --------------------
    const schema = makeExecutableSchema({ typeDefs, resolvers });
    const server = new ApolloServer({ schema });
    await server.start();

    // --------------------
    // HTTP + WebSocket Server
    // --------------------
    const httpServer = createServer();
    const wsServer = new WebSocketServer({ server: httpServer, path: '/graphql' });

    useServer(
      {
        schema,
        onConnect: (ctx) => console.log('🔌 Client connected:', ctx.connectionParams),
        onDisconnect: (ctx, code, reason) =>
          console.log(`❌ Client disconnected: code=${code}, reason=${reason}`),
      },
      wsServer
    );

    // --------------------
    // Start HTTP server
    // --------------------
    httpServer.listen(5001, () => {
      console.log('🚀 Subscription server running at ws://localhost:5001/graphql');
    });
  } catch (err) {
    console.error('❌ Failed to start server:', err);
  }
}

// Start everything
startServer();
