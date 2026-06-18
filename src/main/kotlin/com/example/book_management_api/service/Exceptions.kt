package com.example.book_management_api.service

class NotFoundException(message: String) : RuntimeException(message)

class BadRequestException(message: String) : RuntimeException(message)
