package com.example.book_management_api.dtos

import java.time.LocalDate

/**
 * 著者APIのレスポンスDTO。
 * 登録・更新後に返す著者情報の形を定義する。
 */
data class AuthorResponse(
    val id: Long,
    val name: String,
    val birthDate: LocalDate,
)
