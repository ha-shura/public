package com.example.book_management_api.domain

/**
 * 書籍を表すドメインモデル。
 * DBから取得した書籍本体の情報をアプリケーション内で扱うために利用する。
 */
data class Book(
    val id: Long,
    val title: String,
    val price: Int,
    val publicationStatus: PublicationStatus,
)
