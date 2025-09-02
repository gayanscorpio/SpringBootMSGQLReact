query {
  allStudents {
    id
    name
    email
  }
}

curl -X POST http://localhost:4000/graphql \
-H "Content-Type: application/json" \
-d '{"query":"query { allStudents { id name email } }"}'

--------------------------------------------------------

query {
  studentById(id: "STUDENT_ID_HERE") {
    id
    name
    email
  }
}

curl -X POST http://localhost:4000/graphql \
-H "Content-Type: application/json" \
-d '{"query":"query { studentById(id: \"STUDENT_ID_HERE\") { id name email } }"}'

--------------------------------------------------------------------------------

mutation {
  addStudent(name: "John Doe", email: "john.doe@example.com") {
    id
    name
    email
  }
}

curl -X POST http://localhost:4000/graphql \
-H "Content-Type: application/json" \
-d '{"query":"mutation { addStudent(name: \"John Doe\", email: \"john.doe@example.com\") { id name email } }"}'

----------------------------------------------------------------------------------------------------------------

mutation {
  updateStudent(id: "STUDENT_ID_HERE", name: "Jane Doe", email: "jane.doe@example.com") {
    id
    name
    email
  }
}


curl -X POST http://localhost:4000/graphql \
-H "Content-Type: application/json" \
-d '{"query":"mutation { updateStudent(id: \"STUDENT_ID_HERE\", name: \"Jane Doe\", email: \"jane.doe@example.com\") { id name email } }"}'

-------------------------------------------------------------------------------------------------------------------------------------------

mutation {
  deleteStudent(id: "STUDENT_ID_HERE")
}

curl -X POST http://localhost:4000/graphql \
-H "Content-Type: application/json" \
-d '{"query":"mutation { deleteStudent(id: \"STUDENT_ID_HERE\") }"}'


















