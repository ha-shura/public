package com.example.book_management_api.dtos

import com.example.book_management_api.domain.Author
import com.example.book_management_api.domain.Book
import com.example.book_management_api.domain.BookWithAuthorIds

/**
 * ドメインモデルをAPIレスポンスDTOへ変換するMapper。
 * ControllerやRepositoryに変換処理を散らさないために利用する。
 */
fun Author.toResponse(): AuthorResponse =
    AuthorResponse(
        id = id,
        name = name,
        birthDate = birthDate,
    )

fun Book.toResponse(): BookResponse =
    BookResponse(
        id = id,
        title = title,
        price = price,
        publicationStatus = publicationStatus,
    )

fun BookWithAuthorIds.toSaveResponse(): BookSaveResponse =
    BookSaveResponse(
        id = book.id,
        title = book.title,
        price = book.price,
        publicationStatus = book.publicationStatus,
        authorIds = authorIds,
    )
