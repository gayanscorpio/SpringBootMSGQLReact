curl -i -X POST http://localhost:8081/graphql \
  -H "Content-Type: application/json" \
  -d '{
    "query": "query { allBooks { id title author totalCopies availableCopies borrowedBy { id, borrowedBooksCount } } }"
  }'
  
  
  