package com.example.book_management_api.repository

import com.example.bookmanagement.jooq.tables.references.BOOKS
import com.example.book_management_api.domain.Book
import com.example.book_management_api.domain.PublicationStatus
import com.example.book_management_api.domain.toPublicationStatus
import org.jooq.DSLContext
import org.jooq.Record
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

/**
 * 書籍テーブルへのDBアクセスを担当するRepository。
 * jOOQを利用して書籍本体の登録・更新・検索を行う。
 */
@Repository
class BookRepository(
    private val dsl: DSLContext,
) {
    fun create(
        title: String,
        price: Int,
        publicationStatus: PublicationStatus,
    ): Book {
        val id = dsl.insertInto(BOOKS)
            .columns(BOOKS.TITLE, BOOKS.PRICE, BOOKS.PUBLICATION_STATUS)
            .values(title, price, publicationStatus.name)
            .returningResult(BOOKS.ID)
            .fetchOne(BOOKS.ID)
            ?: throw IllegalStateException("failed to create book")

        return findById(id) ?: throw IllegalStateException("created book not found: $id")
    }

    fun update(
        id: Long,
        title: String,
        price: Int,
        publicationStatus: PublicationStatus,
    ): Book? {
        val updated = dsl.update(BOOKS)
            .set(BOOKS.TITLE, title)
            .set(BOOKS.PRICE, price)
            .set(BOOKS.PUBLICATION_STATUS, publicationStatus.name)
            .set(BOOKS.UPDATED_AT, LocalDateTime.now())
            .where(BOOKS.ID.eq(id))
            .execute()

        return if (updated == 0) null else findById(id)
    }

    fun findById(id: Long): Book? =
        dsl.select(
            BOOKS.ID,
            BOOKS.TITLE,
            BOOKS.PRICE,
            BOOKS.PUBLICATION_STATUS,
        )
            .from(BOOKS)
            .where(BOOKS.ID.eq(id))
            .fetchOne { it.toBook() }

    fun findByIds(ids: Collection<Long>): List<Book> {
        if (ids.isEmpty()) return emptyList()

        return dsl.select(
            BOOKS.ID,
            BOOKS.TITLE,
            BOOKS.PRICE,
            BOOKS.PUBLICATION_STATUS,
        )
            .from(BOOKS)
            .where(BOOKS.ID.`in`(ids))
            .orderBy(BOOKS.ID.asc())
            .fetch { it.toBook() }
    }

    private fun Record.toBook(): Book =
        Book(
            id = get(BOOKS.ID) ?: throw IllegalStateException("book id is null"),
            title = get(BOOKS.TITLE) ?: throw IllegalStateException("book title is null"),
            price = get(BOOKS.PRICE) ?: throw IllegalStateException("book price is null"),
            publicationStatus = get(BOOKS.PUBLICATION_STATUS).toPublicationStatus(),
        )
}
