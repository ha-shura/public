package com.example.book_management_api.service

import com.example.book_management_api.dtos.AuthorRequest
import com.example.book_management_api.dtos.AuthorResponse
import com.example.book_management_api.dtos.BookResponse
import com.example.book_management_api.dtos.toResponse
import com.example.book_management_api.repository.AuthorRepository
import com.example.book_management_api.repository.BookAuthorRepository
import com.example.book_management_api.repository.BookRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.format.DateTimeParseException

@Service
class AuthorService(
    private val authorRepository: AuthorRepository,
    private val bookRepository: BookRepository,
    private val bookAuthorRepository: BookAuthorRepository,
) {
    @Transactional
    fun create(request: AuthorRequest): AuthorResponse {
        val birthDate = parseBirthDate(request.birthDate)

        return authorRepository.create(
            name = request.name,
            birthDate = birthDate,
        ).toResponse()
    }

    @Transactional
    fun update(id: Long, request: AuthorRequest): AuthorResponse {
        val birthDate = parseBirthDate(request.birthDate)

        return authorRepository.update(
            id = id,
            name = request.name,
            birthDate = birthDate,
        )?.toResponse() ?: throw NotFoundException("著者が見つかりません: $id")
    }

    @Transactional(readOnly = true)
    fun findBooks(id: Long): List<BookResponse> {
        // 存在しない著者IDでは空配列を返さず、明示的に404を返す。
        authorRepository.findById(id) ?: throw NotFoundException("著者が見つかりません: $id")

        val bookIds = bookAuthorRepository.findBookIdsByAuthorId(id)
        return bookRepository.findByIds(bookIds).map { it.toResponse() }
    }

    private fun parseBirthDate(value: String): LocalDate {
        val birthDate = try {
            LocalDate.parse(value)
        } catch (exception: DateTimeParseException) {
            throw BadRequestException("生年月日は実在する日付をyyyy-MM-dd形式で入力してください")
        }

        if (birthDate.isAfter(LocalDate.now())) {
            throw BadRequestException("生年月日は現在日以前の日付を入力してください")
        }

        return birthDate
    }
}
