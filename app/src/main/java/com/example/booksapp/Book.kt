package com.example.booksapp

import kotlinx.serialization.Serializable

@Serializable
data class Book(
    var author: MutableList<Author> = mutableListOf(),
    val title: String,
    var year: UInt = 0u,
    var genre: String = "undefined"
)

@Serializable
data class BookRequest(
    val author: String?,
    val title: String?,
    val genre: String?
)

@Serializable
data class Author(
    val key: String = "undefined",
    val name: String
)
