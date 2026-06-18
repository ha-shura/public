package com.example.book_management_api.api.author

import com.example.book_management_api.dtos.AuthorRequest
import com.example.book_management_api.dtos.AuthorResponse
import com.example.book_management_api.dtos.BookResponse
import com.example.book_management_api.service.AuthorService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

/**
 * 著者APIのコントローラー。
 * 著者の登録・更新と、著者に紐づく書籍取得のHTTP入口を定義する。
 */
@RestController
@RequestMapping("/api/v1/authors")
class AuthorController(
    private val authorService: AuthorService,
) {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@Valid @RequestBody request: AuthorRequest): AuthorResponse =
        authorService.create(request)

    @PutMapping("/{id}")
    fun update(
        @PathVariable id: Long,
        @Valid @RequestBody request: AuthorRequest,
    ): AuthorResponse = authorService.update(id, request)

    @GetMapping("/{id}/books")
    fun books(@PathVariable id: Long): List<BookResponse> =
        authorService.findBooks(id)
}
