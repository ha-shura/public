package com.example.book_management_api.service

import com.example.book_management_api.domain.Book
import com.example.book_management_api.domain.BookWithAuthorIds
import com.example.book_management_api.domain.PublicationStatus
import com.example.book_management_api.dtos.BookRequest
import com.example.book_management_api.dtos.BookSaveResponse
import com.example.book_management_api.dtos.toSaveResponse
import com.example.book_management_api.repository.AuthorRepository
import com.example.book_management_api.repository.BookAuthorRepository
import com.example.book_management_api.repository.BookRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 書籍に関する業務処理を担当するサービス。
 * 著者存在チェックや出版状況変更ルールを含めて、登録・更新処理を制御する。
 */
@Service
class BookService(
    private val authorRepository: AuthorRepository,
    private val bookRepository: BookRepository,
    private val bookAuthorRepository: BookAuthorRepository,
) {
    @Transactional
    fun create(request: BookRequest): BookSaveResponse {
        validateAuthors(request.authorIds)

        val book = bookRepository.create(
            title = request.title,
            price = request.price,
            publicationStatus = request.publicationStatus,
        )
        bookAuthorRepository.create(book.id, request.authorIds)

        return toSaveResponse(book)
    }

    @Transactional
    fun update(id: Long, request: BookRequest): BookSaveResponse {
        validateAuthors(request.authorIds)

        val current = bookRepository.findById(id) ?: throw NotFoundException("書籍が見つかりません: $id")
        // 出版済みの書籍は未出版へ戻せない業務ルール。
        if (current.publicationStatus == PublicationStatus.PUBLISHED &&
            request.publicationStatus == PublicationStatus.UNPUBLISHED
        ) {
            throw BadRequestException("出版済みの書籍は未出版に変更できません")
        }

        val updatedBook = bookRepository.update(
            id = id,
            title = request.title,
            price = request.price,
            publicationStatus = request.publicationStatus,
        ) ?: throw NotFoundException("書籍が見つかりません: $id")
        bookAuthorRepository.replace(id, request.authorIds)

        return toSaveResponse(updatedBook)
    }

    private fun toSaveResponse(book: Book): BookSaveResponse =
        BookWithAuthorIds(
            book = book,
            authorIds = bookAuthorRepository.findAuthorIdsByBookId(book.id),
        ).toSaveResponse()

    private fun validateAuthors(authorIds: List<Long>) {
        // 重複を除いた著者IDで存在確認し、存在しないIDをまとめて返す。
        val distinctIds = authorIds.distinct()
        val existingIds = authorRepository.findByIds(distinctIds).map { it.id }.toSet()
        val missingIds = distinctIds.filterNot { it in existingIds }
        if (missingIds.isNotEmpty()) {
            throw BadRequestException("存在しない著者IDが指定されています: ${missingIds.joinToString(",")}")
        }
    }
}
