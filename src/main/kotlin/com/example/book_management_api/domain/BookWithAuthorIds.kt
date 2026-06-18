package com.example.book_management_api.domain

data class BookWithAuthorIds(
    val book: Book,
    val authorIds: List<Long>,
)
