package com.example.book_management_api.dtos

import com.example.book_management_api.domain.PublicationStatus

/**
 * 書籍登録・更新APIのレスポンスDTO。
 * 登録・更新後の書籍本体と紐づく著者ID一覧を返す。
 */
data class BookSaveResponse(
    val id: Long,
    val title: String,
    val price: Int,
    val publicationStatus: PublicationStatus,
    val authorIds: List<Long>,
)
