package com.example.booksapp

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonElement
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.features.ClientRequestException
import io.ktor.client.features.HttpTimeout
import io.ktor.client.features.RedirectResponseException
import io.ktor.client.features.ServerResponseException
import io.ktor.client.features.defaultRequest
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.client.features.logging.LogLevel
import io.ktor.client.features.logging.Logging
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.URLBuilder
import io.ktor.http.contentType
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement

// Only subjects api calls for now
object ApiRoutes {
    private const val BASE_URL = "openlibrary.org"
    const val SCHEME = "https"
    const val SUBJECTS_URL = "$BASE_URL/subjects"
}

interface ApiService {

    suspend fun getBooks(searchParams: String = "love"): List<Book>

    suspend fun createBookRequest(bookRequest: BookRequest): Book?

    companion object {
        fun create(): ApiService {
            return ApiServiceImpl(
                client = HttpClient(Android) {
                    // Logging
                    install(Logging) {
                        level = LogLevel.ALL
                    }
                    // JSON
                    install(JsonFeature) {
                        serializer = KotlinxSerializer(json)
                        //or serializer = KotlinxSerializer()
                    }
                    // Timeout
                    install(HttpTimeout) {
                        requestTimeoutMillis = 15000L
                        connectTimeoutMillis = 15000L
                        socketTimeoutMillis = 15000L
                    }
                    // Apply to all requests
                    defaultRequest {
                        // Parameter("api_key", "some_api_key")
                        // Content Type
                        if (method != HttpMethod.Get) contentType(ContentType.Application.Json)
                        accept(ContentType.Application.Json)
                    }
                }
            )
        }

        private val json = kotlinx.serialization.json.Json {
            ignoreUnknownKeys = true
            isLenient = true
            encodeDefaults = false
        }
    }
}

class ApiServiceImpl(
    private val client: HttpClient
) : ApiService {

    // TODO search by subject currently is hardcoded
    override suspend fun getBooks(searchParams: String): List<Book> {
        return try {
            val result: JsonObject = client.get(scheme = ApiRoutes.SCHEME, host = "${ApiRoutes.SUBJECTS_URL}/${searchParams}.json")
            val booksList = Json.decodeFromJsonElement<List<JsonObject>>(result["works"]!!)
            parseInfoBook(booksList)
        } catch (ex: RedirectResponseException) {
            Log.e("${this.javaClass} Failed to quarry books - ", "${ex.message}")
            emptyList()
        } catch (ex: ClientRequestException) {
            Log.e("${this.javaClass} Failed to quarry books - ", "${ex.message}")
            emptyList()
        } catch (ex: ServerResponseException) {
            Log.e("${this.javaClass} Failed to quarry books - ", "${ex.message}")
            emptyList()
        }
    }

    override suspend fun createBookRequest(bookRequest: BookRequest): Book? {
        return try {
            client.post<Book> {
                url { URLBuilder("${ApiRoutes.SCHEME}://${ApiRoutes.SUBJECTS_URL}").build() }
                body = bookRequest
            }
        } catch (ex: RedirectResponseException) {
            Log.e("${this.javaClass} Failed to quarry books - ", "${ex.message}")
            null
        } catch (ex: ClientRequestException) {
            Log.e("${this.javaClass} Failed to quarry books - ", "${ex.message}")
            null
        } catch (ex: ServerResponseException) {
            Log.e("${this.javaClass} Failed to quarry books - ", "${ex.message}")
            null
        }
    }

    private fun parseInfoBook(jsonResponseList: List<JsonObject>) : List<Book> {
        return jsonResponseList.map { book ->
            val listAuthors = arrayListOf<Author>()
            val authorsList = book["authors"] as JsonArray?
            authorsList?.forEach {
                listAuthors.add(Gson().fromJson(it.toString(), Author::class.java))
            }
            val newBook = Gson().fromJson(book.toString(), Book::class.java)
            newBook.author = listAuthors
            newBook
        }
    }
}