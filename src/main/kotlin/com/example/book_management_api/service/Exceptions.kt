package com.example.book_management_api.service

/**
 * サービス層で利用する業務例外。
 * ControllerAdviceでHTTPステータスへ変換される。
 */
class NotFoundException(message: String) : RuntimeException(message)

class BadRequestException(message: String) : RuntimeException(message)
