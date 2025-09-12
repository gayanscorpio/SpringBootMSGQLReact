1️⃣ Add a Student (addStudent)

curl -i -X POST http://localhost:8080/graphql \
-H "Content-Type: application/json" \
-H "Authorization: Bearer $TOKEN" \
-d '{"query":"mutation { addStudent(name: \"John Doe\", email: \"john@example.com\") { id name email } }"}'



2️⃣ Update a Student (updateStudent)

STUDENT_ID="replace_with_student_id"

curl -i -X POST http://localhost:8080/graphql \
-H "Content-Type: application/json" \
-H "Authorization: Bearer $TOKEN" \
-d "{\"query\":\"mutation { updateStudent(id: \\\"$STUDENT_ID\\\", name: \\\"John Updated\\\", email: \\\"john.updated@example.com\\\") { id name email } }\"}"



3️⃣ Delete a Student (deleteStudent)

STUDENT_ID="replace_with_student_id"

curl -i -X POST http://localhost:8080/graphql \
-H "Content-Type: application/json" \
-H "Authorization: Bearer $TOKEN" \
-d "{\"query\":\"mutation { deleteStudent(id: \\\"$STUDENT_ID\\\") }\"}"



4️⃣ Query all Students (allStudents)

curl -i -X POST http://localhost:8080/graphql \
-H "Content-Type: application/json" \
-H "Authorization: Bearer $TOKEN" \
-d '{"query":"query { allStudents { id name email } }"}'



5️⃣ Query a Student by ID (studentById)

STUDENT_ID="replace_with_student_id"

curl -i -X POST http://localhost:8080/graphql \
-H "Content-Type: application/json" \
-H "Authorization: Bearer $TOKEN" \
-d "{\"query\":\"query { studentById(id: \\\"$STUDENT_ID\\\") { id name email } }\"}"



6️⃣ Federation: Book service requesting Student entity

Book service resolves borrowedBy → sends _entities query automatically to Student service. For reference, 
this is the automatic federated request Apollo Gateway triggers:

{
  "query": "query($representations: [_Any!]!) { _entities(representations: $representations) { ... on Student { id name email } } }",
  "variables": {
    "representations": [{ "id": "student-id" }]
  }
}


STUDENT_ID="replace_with_student_id"

curl -i -X POST http://localhost:8080/graphql \
-H "Content-Type: application/json" \
-H "Authorization: Bearer $TOKEN" \
-d "{\"query\":\"query(\$representations: [_Any!]!) { _entities(representations: \$representations) { ... on Student { id name email } } }\",\"variables\":{\"representations\":[{\"id\":\"$STUDENT_ID\"}]}}"


