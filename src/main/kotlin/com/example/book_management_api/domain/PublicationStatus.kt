package com.example.book_management_api.domain

/**
 * 書籍の出版状況を表す列挙型。
 * APIやDBで扱う出版状況の値をアプリケーション側で安全に扱う。
 */
enum class PublicationStatus {
    UNPUBLISHED,
    PUBLISHED,
}

fun String?.toPublicationStatus(): PublicationStatus =
    PublicationStatus.valueOf(this ?: throw IllegalStateException("publication status is null"))
