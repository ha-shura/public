package com.example.book_management_api.repository

import com.example.bookmanagement.jooq.tables.references.BOOK_AUTHORS
import org.jooq.DSLContext
import org.springframework.stereotype.Repository

@Repository
class BookAuthorRepository(
    private val dsl: DSLContext,
) {
    fun create(bookId: Long, authorIds: List<Long>) {
        authorIds.distinct().forEach { authorId ->
            dsl.insertInto(BOOK_AUTHORS)
                .columns(BOOK_AUTHORS.BOOK_ID, BOOK_AUTHORS.AUTHOR_ID)
                .values(bookId, authorId)
                .execute()
        }
    }

    fun replace(bookId: Long, authorIds: List<Long>) {
        dsl.deleteFrom(BOOK_AUTHORS)
            .where(BOOK_AUTHORS.BOOK_ID.eq(bookId))
            .execute()

        create(bookId, authorIds)
    }

    fun findAuthorIdsByBookId(bookId: Long): List<Long> =
        dsl.select(BOOK_AUTHORS.AUTHOR_ID)
            .from(BOOK_AUTHORS)
            .where(BOOK_AUTHORS.BOOK_ID.eq(bookId))
            .orderBy(BOOK_AUTHORS.AUTHOR_ID.asc())
            .fetch { it.get(BOOK_AUTHORS.AUTHOR_ID) ?: throw IllegalStateException("author id is null") }

    fun findBookIdsByAuthorId(authorId: Long): List<Long> =
        dsl.select(BOOK_AUTHORS.BOOK_ID)
            .from(BOOK_AUTHORS)
            .where(BOOK_AUTHORS.AUTHOR_ID.eq(authorId))
            .orderBy(BOOK_AUTHORS.BOOK_ID.asc())
            .fetch { it.get(BOOK_AUTHORS.BOOK_ID) ?: throw IllegalStateException("book id is null") }
}
