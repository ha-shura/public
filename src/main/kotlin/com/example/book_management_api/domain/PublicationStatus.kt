package com.example.book_management_api.domain

enum class PublicationStatus {
    UNPUBLISHED,
    PUBLISHED,
}

fun String?.toPublicationStatus(): PublicationStatus =
    PublicationStatus.valueOf(this ?: throw IllegalStateException("publication status is null"))
