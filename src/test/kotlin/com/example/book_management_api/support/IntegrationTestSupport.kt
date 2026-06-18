package com.example.book_management_api.support

import com.example.book_management_api.BookManagementApiApplication
import com.fasterxml.jackson.databind.ObjectMapper
import org.jooq.DSLContext
import org.jooq.impl.DSL.name
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest(classes = [BookManagementApiApplication::class])
@AutoConfigureMockMvc
@ActiveProfiles("test")
abstract class IntegrationTestSupport {
    protected val authorsUrl = "/api/v1/authors"
    protected val booksUrl = "/api/v1/books"

    @Autowired
    protected lateinit var mockMvc: MockMvc

    @Autowired
    protected lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var dsl: DSLContext

    @BeforeEach
    fun cleanDatabase() {
        // テスト間でデータが残らないよう、関連テーブルから順に削除する。
        dsl.deleteFrom(org.jooq.impl.DSL.table(name("book_authors"))).execute()
        dsl.deleteFrom(org.jooq.impl.DSL.table(name("books"))).execute()
        dsl.deleteFrom(org.jooq.impl.DSL.table(name("authors"))).execute()
    }

    protected fun createAuthor(name: String, birthDate: String): Long {
        val response = mockMvc.perform(
            post(authorsUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"name":"$name","birthDate":"$birthDate"}"""),
        )
            .andExpect(status().isCreated)
            .andReturn()
            .response
            .contentAsString

        return objectMapper.readTree(response)["id"].asLong()
    }

    protected fun createBook(
        title: String,
        price: Int,
        publicationStatus: String,
        authorIds: List<Long>,
    ): Long {
        val response = mockMvc.perform(
            post(booksUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        mapOf(
                            "title" to title,
                            "price" to price,
                            "publicationStatus" to publicationStatus,
                            "authorIds" to authorIds,
                        ),
                    ),
                ),
        )
            .andExpect(status().isCreated)
            .andReturn()
            .response
            .contentAsString

        return objectMapper.readTree(response)["id"].asLong()
    }
}
