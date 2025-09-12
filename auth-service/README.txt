//Register User

curl -i  -X POST http://localhost:8082/graphql \
  -H "Content-Type: application/json" \
  -d '{
    "query": "mutation { register(username: \"alice\", password: \"mypassword\", role: \"Student\") }"
  }'


//Login User

curl -i -X POST http://localhost:8082/graphql \
  -H "Content-Type: application/json" \
  -d '{
    "query": "mutation { login(username: \"alice\", password: \"mypassword\") { token userId } }"
  }'
