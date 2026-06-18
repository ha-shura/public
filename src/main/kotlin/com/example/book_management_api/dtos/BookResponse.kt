package com.example.book_management_api.dtos

import com.example.book_management_api.domain.PublicationStatus

data class BookResponse(
    val id: Long,
    val title: String,
    val price: Int,
    val publicationStatus: PublicationStatus,
)
