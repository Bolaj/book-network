package com.timibolaji.book.book;

import com.timibolaji.book.common.PageResponse;
import com.timibolaji.book.exception.OperationNotPermittedException;
import com.timibolaji.book.file.FileStorageService;
import com.timibolaji.book.history.BookTransactionHistory;
import com.timibolaji.book.history.BookTransactionHistoryRepository;
import com.timibolaji.book.user.User;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;


@Service
@RequiredArgsConstructor
public class BookService {
    private final BookRepository bookRepository;
    private final BookMapper bookMapper;
    private final BookTransactionHistoryRepository historyRepository;
    private final BookTransactionHistoryRepository bookTransactionHistoryRepository;
    private final FileStorageService fileStorageService;

    public Integer save(BookRequest request, Authentication connectedUser) {
        User user = ((User) connectedUser.getPrincipal());
        Book book = bookMapper.toBook(request);
        book.setOwner(user);
        return bookRepository.save(book).getId();
    }

    public BookResponse findById(Integer bookId) {
        return bookRepository.findById(bookId)
                .map(bookMapper::toBookResponse)
                .orElseThrow(() -> new EntityNotFoundException("No Book Found With the ID::" + bookId));
    }

    public PageResponse<BookResponse> findAllBooks(int page, int size, Authentication connectedUser) {
        User user = ((User) connectedUser.getPrincipal());
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());
        Page<Book> books = bookRepository.findAllDisplayBooks(pageable, user.getId());
        List<BookResponse> bookResponse = books.stream()
                .map(bookMapper::toBookResponse)
                .toList();

        return new PageResponse<>(
                bookResponse,
                books.getNumber(),
                books.getSize(),
                books.getTotalElements(),
                books.getTotalPages(),
                books.isFirst(),
                books.isLast()
        );
    }

    public PageResponse<BookResponse> findAllBooksByOwner(int page, int size, Authentication connectedUser) {
        User user = ((User) connectedUser.getPrincipal());
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());
        Page<Book> books = bookRepository.findAll(BookSpecification.withOwnerId(user.getId()), pageable);
        List<BookResponse> bookResponse = books.stream()
                .map(bookMapper::toBookResponse)
                .toList();

        return new PageResponse<>(
                bookResponse,
                books.getNumber(),
                books.getSize(),
                books.getTotalElements(),
                books.getTotalPages(),
                books.isFirst(),
                books.isLast()
        );

    }

    public PageResponse<BorrowedBookResponse> findAllBorrowedBooks(int page, int size, Authentication connectedUser) {
        User user = ((User) connectedUser.getPrincipal());
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());
        Page<BookTransactionHistory> allBorrowedBooks = historyRepository.findAllBorrowedBooks(pageable, user.getId());
        List<BorrowedBookResponse> bookResponse = allBorrowedBooks.stream()
                .map(bookMapper::toBorrowedBookResponse)
                .toList();
        return new PageResponse<>(
                bookResponse,
                allBorrowedBooks.getNumber(),
                allBorrowedBooks.getSize(),
                allBorrowedBooks.getTotalElements(),
                allBorrowedBooks.getTotalPages(),
                allBorrowedBooks.isFirst(),
                allBorrowedBooks.isLast()
        );
    }

    public PageResponse<BorrowedBookResponse> findAllReturnedBooks(int page, int size, Authentication connectedUser) {
        User user = ((User) connectedUser.getPrincipal());
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());
        Page<BookTransactionHistory> allBorrowedBooks = historyRepository.findAllReturnedBooks(pageable, user.getId());
        List<BorrowedBookResponse> bookResponse = allBorrowedBooks.stream()
                .map(bookMapper::toBorrowedBookResponse)
                .toList();
        return new PageResponse<>(
                bookResponse,
                allBorrowedBooks.getNumber(),
                allBorrowedBooks.getSize(),
                allBorrowedBooks.getTotalElements(),
                allBorrowedBooks.getTotalPages(),
                allBorrowedBooks.isFirst(),
                allBorrowedBooks.isLast()
        );
    }

    public Integer updateShareableStatus(Integer bookId, Authentication connectedUser) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException("No Book Found with ID:: " + bookId));
        User user = ((User) connectedUser.getPrincipal());
        if(!Objects.equals(book.getOwner().getId(), user.getId())){
            throw new OperationNotPermittedException("You cannot update book shareable status");

        }
        book.setShareable(!book.isShareable());
        bookRepository.save(book);
        return bookId;

    }

    public Integer updateArchivedStatus(Integer bookId, Authentication connectedUser) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException("No Book Found with ID:: " + bookId));
        User user = ((User) connectedUser.getPrincipal());
        if(!Objects.equals(book.getOwner().getId(), user.getId())){
            throw new OperationNotPermittedException("You cannot update book archived status");

        }
        book.setArchived(!book.isArchived());
        bookRepository.save(book);
        return bookId;
    }

    public Integer borrowBook(Integer bookId, Authentication connectedUser) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException("No Book Found with ID:: " + bookId));
        if(book.isArchived() || !book.isShareable()){
            throw new OperationNotPermittedException("The requested book cannot be borrowed");
        }
        User user = ((User) connectedUser.getPrincipal());
        if(Objects.equals(book.getOwner().getId(), user.getId())){
            throw new OperationNotPermittedException("You cannot borrow your own book");

        }
        final boolean isAlreadyBorrowed =  historyRepository.isAlreadyBorrowed(bookId, user.getId());
        if(isAlreadyBorrowed){
            throw new OperationNotPermittedException("The requested book is already borrowed.");
        }
        BookTransactionHistory bookTransactionHistory = BookTransactionHistory.builder()
                .user(user)
                .book(book)
                .returned(false)
                .returnedApproved(false)
                .build();
        return historyRepository.save(bookTransactionHistory).getId();

    }

    public Integer returnBorrowedBook(Integer bookId, Authentication connectedUser) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException("No Book Found with ID:: " + bookId));
        if(book.isArchived() || !book.isShareable()){
            throw new OperationNotPermittedException("The requested book cannot be borrowed");
        }
        User user = ((User) connectedUser.getPrincipal());
        if(Objects.equals(book.getOwner().getId(), user.getId())){
            throw new OperationNotPermittedException("You cannot borrow or return your own book");

        }
        BookTransactionHistory bookTransactionHistory = bookTransactionHistoryRepository.findByBookIdAndUserId(bookId, user.getId())
                .orElseThrow(() -> new OperationNotPermittedException("You did not borrow this book"));
        bookTransactionHistory.setReturned(true);
        return historyRepository.save(bookTransactionHistory).getId();

    }

    public Integer approveReturnedBorrowedBook(Integer bookId, Authentication connectedUser) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException("No Book Found with ID:: " + bookId));
        if(book.isArchived() || !book.isShareable()){
            throw new OperationNotPermittedException("The requested book cannot be borrowed");
        }
        User user = ((User) connectedUser.getPrincipal());
        if(Objects.equals(book.getOwner().getId(), user.getId())){
            throw new OperationNotPermittedException("You cannot borrow or return your own book");

        }
        BookTransactionHistory bookTransactionHistory = bookTransactionHistoryRepository.findByBookIdAndOwnerId(bookId, user.getId())
                .orElseThrow(() -> new OperationNotPermittedException("The Book is returned yet. You cannot approve yet"));
        bookTransactionHistory.setReturnedApproved(true);
        return historyRepository.save(bookTransactionHistory).getId();

    }

    public void uploadBookCover(MultipartFile file, Integer bookId, Authentication connectedUser) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException("No Book Found with ID:: " + bookId));
        User user = ((User) connectedUser.getPrincipal());
        var bookCover = fileStorageService.saveFile(file,user.getId());
        book.setBookCover(bookCover);
        bookRepository.save(book);
    }
}
