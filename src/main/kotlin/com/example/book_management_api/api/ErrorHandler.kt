package com.example.book_management_api.api

import com.example.book_management_api.service.BadRequestException
import com.example.book_management_api.service.NotFoundException
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

/**
 * API共通の例外ハンドラー。
 * 入力チェックエラーや業務エラーを、利用者向けのJSONレスポンスへ変換する。
 */
@RestControllerAdvice
class ErrorHandler {
    @ExceptionHandler(NotFoundException::class)
    fun notFound(exception: NotFoundException): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse(exception.message ?: "対象データが見つかりません"))

    @ExceptionHandler(BadRequestException::class)
    fun badRequest(exception: BadRequestException): ResponseEntity<ErrorResponse> =
        ResponseEntity.badRequest().body(ErrorResponse(exception.message ?: "リクエスト内容が不正です"))

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun validation(exception: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val message = exception.bindingResult.fieldErrors
            .firstOrNull()
            ?.let { "${it.field}: ${it.defaultMessage}" }
            ?: "入力値が不正です"
        return ResponseEntity.badRequest().body(ErrorResponse(message))
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun invalidJson(): ResponseEntity<ErrorResponse> =
        ResponseEntity.badRequest().body(ErrorResponse("リクエストボディの形式が不正です"))

    @ExceptionHandler(DataIntegrityViolationException::class)
    fun dataIntegrity(): ResponseEntity<ErrorResponse> =
        ResponseEntity.badRequest().body(ErrorResponse("データベース制約に違反しています"))
}

data class ErrorResponse(
    val message: String,
)
