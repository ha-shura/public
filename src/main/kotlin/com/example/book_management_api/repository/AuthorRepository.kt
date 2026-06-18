package com.example.book_management_api.repository

import com.example.bookmanagement.jooq.tables.references.AUTHORS
import com.example.book_management_api.domain.Author
import org.jooq.DSLContext
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * 著者テーブルへのDBアクセスを担当するRepository。
 * jOOQを利用して著者の登録・更新・検索を行う。
 */
@Repository
class AuthorRepository(
    private val dsl: DSLContext,
) {
    fun create(name: String, birthDate: LocalDate): Author {
        val id = dsl.insertInto(AUTHORS)
            .columns(AUTHORS.NAME, AUTHORS.BIRTH_DATE)
            .values(name, birthDate)
            .returningResult(AUTHORS.ID)
            .fetchOne(AUTHORS.ID)
            ?: throw IllegalStateException("failed to create author")

        return findById(id) ?: throw IllegalStateException("created author not found: $id")
    }

    fun update(id: Long, name: String, birthDate: LocalDate): Author? {
        val updated = dsl.update(AUTHORS)
            .set(AUTHORS.NAME, name)
            .set(AUTHORS.BIRTH_DATE, birthDate)
            .set(AUTHORS.UPDATED_AT, LocalDateTime.now())
            .where(AUTHORS.ID.eq(id))
            .execute()

        return if (updated == 0) null else findById(id)
    }

    fun findById(id: Long): Author? =
        dsl.select(AUTHORS.ID, AUTHORS.NAME, AUTHORS.BIRTH_DATE)
            .from(AUTHORS)
            .where(AUTHORS.ID.eq(id))
            .fetchOne {
                Author(
                    id = it.get(AUTHORS.ID) ?: throw IllegalStateException("author id is null"),
                    name = it.get(AUTHORS.NAME) ?: throw IllegalStateException("author name is null"),
                    birthDate = it.get(AUTHORS.BIRTH_DATE) ?: throw IllegalStateException("author birth date is null"),
                )
            }

    fun findByIds(ids: Collection<Long>): List<Author> {
        if (ids.isEmpty()) return emptyList()

        return dsl.select(AUTHORS.ID, AUTHORS.NAME, AUTHORS.BIRTH_DATE)
            .from(AUTHORS)
            .where(AUTHORS.ID.`in`(ids))
            .fetch {
                Author(
                    id = it.get(AUTHORS.ID) ?: throw IllegalStateException("author id is null"),
                    name = it.get(AUTHORS.NAME) ?: throw IllegalStateException("author name is null"),
                    birthDate = it.get(AUTHORS.BIRTH_DATE) ?: throw IllegalStateException("author birth date is null"),
                )
            }
    }
}
