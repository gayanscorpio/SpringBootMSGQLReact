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

  
//Register user with the Phone number
  
curl -X POST http://localhost:8082/graphql \
  -H "Content-Type: application/json" \
  -d '{
    "query": "mutation Register($username: String!, $password: String!, $phone: String!) { registerWithPhone(username: $username, password: $password, phone: $phone) }",
    "variables": {
      "username": "alice",
      "password": "alice123",
      "phone": "+94763552581"
    }
  }'


//Veryfy user with OTP code

curl -X POST http://localhost:8082/graphql \
  -H "Content-Type: application/json" \
  -d '{"query": "mutation { verifyPhone(username: \"alice123\", code: \"670548\") { token userId role } }"}'
