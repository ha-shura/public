package com.example.book_management_api.domain

data class Book(
    val id: Long,
    val title: String,
    val price: Int,
    val publicationStatus: PublicationStatus,
)
