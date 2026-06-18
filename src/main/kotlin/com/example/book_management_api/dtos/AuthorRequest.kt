package com.example.book_management_api.dtos

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class AuthorRequest(
    @field:NotBlank(message = "著者名を入力してください")
    @field:Size(max = 255, message = "著者名は255文字以内で入力してください")
    val name: String,
    @field:NotBlank(message = "生年月日を入力してください")
    @field:Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "生年月日はyyyy-MM-dd形式で入力してください")
    val birthDate: String,
)
