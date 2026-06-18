package com.example.book_management_api.dtos

import com.example.book_management_api.domain.PublicationStatus
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size

data class BookRequest(
    @field:NotBlank(message = "タイトルを入力してください")
    @field:Size(max = 255, message = "タイトルは255文字以内で入力してください")
    val title: String,
    @field:Min(value = 0, message = "価格は0以上で入力してください")
    @field:NotNull(message = "価格を入力してください")
    val price: Int,
    @field:NotNull(message = "出版状況を入力してください")
    val publicationStatus: PublicationStatus,
    @field:NotEmpty(message = "著者を1人以上指定してください")
    val authorIds: List<@Positive(message = "著者IDは1以上で指定してください") Long>,
)
