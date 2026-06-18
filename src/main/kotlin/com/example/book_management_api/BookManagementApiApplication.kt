package com.example.book_management_api

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

/**
 * 書籍管理APIのSpring Boot起動クラス。
 * アプリケーション全体のエントリーポイントを定義する。
 */
@SpringBootApplication
class BookManagementApiApplication

fun main(args: Array<String>) {
	runApplication<BookManagementApiApplication>(*args)
}
