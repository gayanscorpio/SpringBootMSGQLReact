curl -X POST http://localhost:8081/graphql \
-H "Content-Type: application/json" \
-d '{"query":"query { allBooks { id title author totalCopies availableCopies borrowedBy { id borrowedBooksCount } } }"}'



curl -X POST http://localhost:8081/graphql \
-H "Content-Type: application/json" \
-d '{"query":"mutation { addBook(title: \"The Great Gatsby\", author: \"F. Scott Fitzgerald\", totalCopies: 5) { id title author totalCopies availableCopies } }"}'



curl -X POST http://localhost:8081/graphql \
-H "Content-Type: application/json" \
-d "{\"query\":\"mutation { updateBook(id: \\\"$BOOK_ID\\\", title: \\\"The Great Gatsby Updated\\\", author: \\\"F. Scott Fitzgerald\\\", totalCopies: 10) { id title author totalCopies availableCopies } }\"}"




curl -X POST http://localhost:8081/graphql \
-H "Content-Type: application/json" \
-d "{\"query\":\"mutation { deleteBook(id: \\\"$BOOK_ID\\\") }\"}"


-------------------------------------------------------------------

BOOK_ID="replace_with_book_id"
STUDENT_ID="replace_with_student_id"

curl -X POST http://localhost:8081/graphql \
-H "Content-Type: application/json" \
-d "{\"query\":\"mutation { borrowBook(bookId: \\\"$BOOK_ID\\\", studentId: \\\"$STUDENT_ID\\\") { id title author totalCopies availableCopies borrowedBy { id borrowedBooksCount } } }\"}"



BOOK_ID="replace_with_book_id"

curl -X POST http://localhost:8081/graphql \
-H "Content-Type: application/json" \
-d "{\"query\":\"mutation { returnBook(bookId: \\\"$BOOK_ID\\\") { id title author totalCopies availableCopies borrowedBy { id borrowedBooksCount } } }\"}"





