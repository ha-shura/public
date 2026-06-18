package com.example.book_management_api.api

import com.example.book_management_api.support.IntegrationTestSupport
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

/**
 * 書籍APIの結合テスト。
 * 書籍の登録・更新・著者紐づけ・業務ルール違反を検証する。
 */
class BookApiIntegrationTest : IntegrationTestSupport() {
    @Test
    fun `creates a book`() {
        val authorId = createAuthor("Natsume Soseki", "1867-02-09")

        mockMvc.perform(
            post(booksUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        mapOf(
                            "title" to "Kokoro",
                            "price" to 1200,
                            "publicationStatus" to "PUBLISHED",
                            "authorIds" to listOf(authorId),
                        ),
                    ),
                ),
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.title").value("Kokoro"))
            .andExpect(jsonPath("$.publicationStatus").value("PUBLISHED"))
            .andExpect(jsonPath("$.authorIds", hasSize<Any>(1)))
            .andExpect(jsonPath("$.authorIds[0]").value(authorId))
    }

    @Test
    fun `updates a book with multiple authors`() {
        val firstAuthorId = createAuthor("Author One", "1980-01-01")
        val secondAuthorId = createAuthor("Author Two", "1981-01-01")
        val bookId = createBook("Draft", 100, "UNPUBLISHED", listOf(firstAuthorId))

        mockMvc.perform(
            put("$booksUrl/$bookId")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        mapOf(
                            "title" to "Finished Book",
                            "price" to 150,
                            "publicationStatus" to "PUBLISHED",
                            "authorIds" to listOf(firstAuthorId, secondAuthorId),
                        ),
                    ),
                ),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.title").value("Finished Book"))
            .andExpect(jsonPath("$.publicationStatus").value("PUBLISHED"))
            .andExpect(jsonPath("$.authorIds", hasSize<Any>(2)))
    }

    @Test
    fun `rejects changing published book to unpublished`() {
        val authorId = createAuthor("Published Author", "1970-01-01")
        val bookId = createBook("Published", 100, "PUBLISHED", listOf(authorId))

        mockMvc.perform(
            put("$booksUrl/$bookId")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        mapOf(
                            "title" to "Published",
                            "price" to 100,
                            "publicationStatus" to "UNPUBLISHED",
                            "authorIds" to listOf(authorId),
                        ),
                    ),
                ),
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").value("出版済みの書籍は未出版に変更できません"))
    }

    @Test
    fun `rejects negative book price`() {
        val authorId = createAuthor("Price Checker", "1980-01-01")

        mockMvc.perform(
            post(booksUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        mapOf(
                            "title" to "Invalid",
                            "price" to -1,
                            "publicationStatus" to "UNPUBLISHED",
                            "authorIds" to listOf(authorId),
                        ),
                    ),
                ),
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").value("price: 価格は0以上で入力してください"))
    }

    @Test
    fun `rejects book without authors`() {
        mockMvc.perform(
            post(booksUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        mapOf(
                            "title" to "No Author",
                            "price" to 100,
                            "publicationStatus" to "UNPUBLISHED",
                            "authorIds" to emptyList<Long>(),
                        ),
                    ),
                ),
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").value("authorIds: 著者を1人以上指定してください"))
    }

    @Test
    fun `rejects too long book title`() {
        val authorId = createAuthor("Title Checker", "1980-01-01")

        mockMvc.perform(
            post(booksUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        mapOf(
                            "title" to "a".repeat(256),
                            "price" to 100,
                            "publicationStatus" to "UNPUBLISHED",
                            "authorIds" to listOf(authorId),
                        ),
                    ),
                ),
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").value("title: タイトルは255文字以内で入力してください"))
    }

    @Test
    fun `rejects invalid author id`() {
        mockMvc.perform(
            post(booksUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        mapOf(
                            "title" to "Invalid Author Id",
                            "price" to 100,
                            "publicationStatus" to "UNPUBLISHED",
                            "authorIds" to listOf(0),
                        ),
                    ),
                ),
        )
            .andExpect(status().isBadRequest)
    }
}
