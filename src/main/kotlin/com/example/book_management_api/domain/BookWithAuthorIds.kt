package com.example.book_management_api.domain

/**
 * 書籍と著者ID一覧をまとめて扱うドメインモデル。
 * 書籍登録・更新後のレスポンス生成で利用する。
 */
data class BookWithAuthorIds(
    val book: Book,
    val authorIds: List<Long>,
)
