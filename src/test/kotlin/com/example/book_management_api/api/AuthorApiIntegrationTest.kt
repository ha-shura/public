package com.example.book_management_api.api

import com.example.book_management_api.support.IntegrationTestSupport
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

/**
 * 著者APIの結合テスト。
 * 著者の登録・更新・入力チェックと、著者に紐づく書籍取得を検証する。
 */
class AuthorApiIntegrationTest : IntegrationTestSupport() {
    @Test
    fun `updates an author`() {
        val authorId = createAuthor("Old Name", "1990-01-01")

        mockMvc.perform(
            put("$authorsUrl/$authorId")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"name":"New Name","birthDate":"1991-02-03"}"""),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(authorId))
            .andExpect(jsonPath("$.name").value("New Name"))
            .andExpect(jsonPath("$.birthDate").value("1991-02-03"))
    }

    @Test
    fun `gets books by author`() {
        val authorId = createAuthor("Natsume Soseki", "1867-02-09")
        createBook("Kokoro", 1200, "PUBLISHED", listOf(authorId))

        mockMvc.perform(get("$authorsUrl/$authorId/books"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$", hasSize<Any>(1)))
            .andExpect(jsonPath("$[0].title").value("Kokoro"))
            .andExpect(jsonPath("$[0].price").value(1200))
            .andExpect(jsonPath("$[0].publicationStatus").value("PUBLISHED"))
    }

    @Test
    fun `rejects author birth date in future`() {
        mockMvc.perform(
            post(authorsUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"name":"Future Author","birthDate":"2999-01-01"}"""),
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `rejects too long author name`() {
        mockMvc.perform(
            post(authorsUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        mapOf(
                            "name" to "a".repeat(256),
                            "birthDate" to "1990-01-01",
                        ),
                    ),
                ),
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").value("name: 著者名は255文字以内で入力してください"))
    }

    @Test
    fun `rejects birth date with time`() {
        mockMvc.perform(
            post(authorsUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"name":"Invalid Date","birthDate":"1990-01-01T10:30:00"}"""),
        )
            .andExpect(status().isBadRequest)
    }
}
