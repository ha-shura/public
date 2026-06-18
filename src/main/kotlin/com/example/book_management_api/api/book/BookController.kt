package com.example.book_management_api.api.book

import com.example.book_management_api.dtos.BookRequest
import com.example.book_management_api.dtos.BookSaveResponse
import com.example.book_management_api.service.BookService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/books")
class BookController(
    private val bookService: BookService,
) {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@Valid @RequestBody request: BookRequest): BookSaveResponse =
        bookService.create(request)

    @PutMapping("/{id}")
    fun update(
        @PathVariable id: Long,
        @Valid @RequestBody request: BookRequest,
    ): BookSaveResponse = bookService.update(id, request)
}
