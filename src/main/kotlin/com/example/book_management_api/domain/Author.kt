package com.example.book_management_api.domain

import java.time.LocalDate

/**
 * 著者を表すドメインモデル。
 * DBから取得した著者情報をアプリケーション内で扱うために利用する。
 */
data class Author(
    val id: Long,
    val name: String,
    val birthDate: LocalDate,
)
