package com.timibolaji.book.book;

import com.timibolaji.book.common.PageResponse;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("books")
@RequiredArgsConstructor
@Tag(name = "Book")
public class BookController {
    private final BookService bookService;

    @PostMapping
    public ResponseEntity<Integer> saveBook(@Valid @RequestBody BookRequest request, Authentication connectedUser) {
        return ResponseEntity.ok(bookService.save(request, connectedUser));
    }

    @GetMapping("{book-id}")
    public ResponseEntity<BookResponse> findBookById(@PathVariable("book-id") Integer bookId) {
        return ResponseEntity.ok(bookService.findById(bookId));
    }

    @GetMapping
    public ResponseEntity<PageResponse<BookResponse>> findAllBooks(
            @RequestParam(name = "page", defaultValue = "0", required = false) int page,
            @RequestParam(name = "size", defaultValue = "10", required = false) int size,
            Authentication connectedUser) {
        return ResponseEntity.ok(bookService.findAllBooks(page, size, connectedUser));

    }

    @GetMapping("/owner")
    public ResponseEntity<PageResponse<BookResponse>> findAllBooksByOwner(
            @RequestParam(name = "page", defaultValue = "0", required = false) int page,
            @RequestParam(name = "size", defaultValue = "10", required = false) int size,
            Authentication connectedUser
    ) {
        return ResponseEntity.ok(bookService.findAllBooksByOwner(page, size, connectedUser));

    }
    @GetMapping("/borrowed")
    public ResponseEntity<PageResponse<BorrowedBookResponse>> findAllBorrowedBooks(
            @RequestParam(name = "page", defaultValue = "0", required = false) int page,
            @RequestParam(name = "size", defaultValue = "10", required = false) int size,
            Authentication connectedUser
    ) {
        return ResponseEntity.ok(bookService.findAllBorrowedBooks(page, size, connectedUser));

    }
    @GetMapping("/returned")
    public ResponseEntity<PageResponse<BorrowedBookResponse>> findAllReturnedBooks(
            @RequestParam(name = "page", defaultValue = "0", required = false) int page,
            @RequestParam(name = "size", defaultValue = "10", required = false) int size,
            Authentication connectedUser
    ) {
        return ResponseEntity.ok(bookService.findAllReturnedBooks(page, size, connectedUser));

    }
    @PatchMapping("/shareable/{book-id}")
    public ResponseEntity<Integer> updateShareableStatus(
            @PathVariable("book-id") Integer bookId,
            Authentication connectedUser
    ){
        return ResponseEntity.ok(bookService.updateShareableStatus(bookId, connectedUser));

    }
    @PatchMapping("/archived/{book-id}")
    public ResponseEntity<Integer> updateArchivedStatus(
            @PathVariable("book-id") Integer bookId,
            Authentication connectedUser
    ){
        return ResponseEntity.ok(bookService.updateArchivedStatus(bookId, connectedUser));

    }
    @PostMapping("/borrow/{book-id}")
    public ResponseEntity<Integer> borrowBook(@PathVariable("book-id") Integer bookId, Authentication connectedUser){
        return ResponseEntity.ok(bookService.borrowBook(bookId, connectedUser));
    }
    @PutMapping("/borrow/return/{book-id}")
    public ResponseEntity<Integer> returnBorrowedBook(@PathVariable("book-id") Integer bookId, Authentication connectedUser){
        return ResponseEntity.ok(bookService.returnBorrowedBook(bookId, connectedUser));
    }
    @PutMapping("/borrow/return/approve/{book-id}")
    public ResponseEntity<Integer> approveReturnedBorrowedBook(@PathVariable("book-id") Integer bookId, Authentication connectedUser){
        return ResponseEntity.ok(bookService.approveReturnedBorrowedBook(bookId, connectedUser));
    }
    @PutMapping("/cover/{book-id}")
    public ResponseEntity<Integer> uploadBookCover(@PathVariable("book-id") Integer bookId, @Parameter() @RequestPart("file") MultipartFile file, Authentication connectedUser){
        bookService.uploadBookCover(file,bookId,connectedUser);
        return ResponseEntity.accepted().build();
    }



}
