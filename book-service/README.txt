1️⃣ Query all books

curl -i -X POST http://localhost:8081/graphql \
-H "Content-Type: application/json" \
-H "Authorization: Bearer $TOKEN" \
-d '{"query":"query { allBooks { id title author totalCopies availableCopies borrowedBy { id borrowedBooksCount } } }"}'



2️⃣ Add a book

curl -i -X POST http://localhost:8081/graphql \
-H "Content-Type: application/json" \
-H "Authorization: Bearer $TOKEN" \
-d '{"query":"mutation { addBook(title: \"The Great Gatsby\", author: \"F. Scott Fitzgerald\", totalCopies: 5) { id title author totalCopies availableCopies } }"}'



3️⃣ Update a book

BOOK_ID="replace_with_book_id"

curl -i -X POST http://localhost:8081/graphql \
-H "Content-Type: application/json" \
-H "Authorization: Bearer $TOKEN" \
-d "{\"query\":\"mutation { updateBook(id: \\\"$BOOK_ID\\\", title: \\\"The Great Gatsby Updated\\\", author: \\\"F. Scott Fitzgerald\\\", totalCopies: 10) { id title author totalCopies availableCopies } }\"}"



4️⃣ Delete a book

BOOK_ID="replace_with_book_id"

curl -i -X POST http://localhost:8081/graphql \
-H "Content-Type: application/json" \
-H "Authorization: Bearer $TOKEN" \
-d "{\"query\":\"mutation { deleteBook(id: \\\"$BOOK_ID\\\") }\"}"



5️⃣ Borrow a book

BOOK_ID="replace_with_book_id"
STUDENT_ID="replace_with_student_id"

curl -i -X POST http://localhost:8081/graphql \
-H "Content-Type: application/json" \
-H "Authorization: Bearer $TOKEN" \
-d "{\"query\":\"mutation { borrowBook(bookId: \\\"$BOOK_ID\\\", studentId: \\\"$STUDENT_ID\\\") { id title author totalCopies availableCopies borrowedBy { id borrowedBooksCount } } }\"}"



6️⃣ Return a book

BOOK_ID="replace_with_book_id"
STUDENT_ID="replace_with_student_id"

curl -i -X POST http://localhost:8081/graphql \
-H "Content-Type: application/json" \
-H "Authorization: Bearer $TOKEN" \
-d "{\"query\":\"mutation { returnBook(bookId: \\\"$BOOK_ID\\\", studentId: \\\"$STUDENT_ID\\\") { id title author totalCopies availableCopies borrowedBy { id borrowedBooksCount } } }\"}"




