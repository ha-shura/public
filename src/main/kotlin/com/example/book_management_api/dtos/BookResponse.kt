package com.example.book_management_api.dtos

import com.example.book_management_api.domain.PublicationStatus

/**
 * 著者に紐づく書籍取得APIのレスポンスDTO。
 * 書籍一覧表示に必要な書籍本体の情報を定義する。
 */
data class BookResponse(
    val id: Long,
    val title: String,
    val price: Int,
    val publicationStatus: PublicationStatus,
)
