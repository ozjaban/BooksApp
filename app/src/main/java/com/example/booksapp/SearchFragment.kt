package com.example.booksapp

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.booksapp.databinding.FragmentSearchBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.decodeFromStream
import java.io.Serializable

/**
 * We call rest api for getting a list of books by search results
 * Probs need some sort of search logic
 * TODO:
 *  - remove button from this fragment that randomly goes to details fragment
 */
class SearchFragment : Fragment() {
    private val apiService by lazy { ApiService.create() }
    private var _binding: FragmentSearchBinding? = null
    private var listOfBooks = listOf<Book>()

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        savedInstanceState?.let {
            val books = it.getString("searched_books")
            listOfBooks = Json.decodeFromString(books!!)
        }

        val booksListAdapter = BooksListAdapter(mutableListOf(), this)
        val recyclerView = view.findViewById<RecyclerView>(R.id.rv_search_list)
        recyclerView?.layoutManager = LinearLayoutManager(context)
        recyclerView?.adapter = booksListAdapter
        booksListAdapter.books = listOfBooks
        val searchBox = view.findViewById<EditText>(R.id.search_text)

        booksListAdapter.onItemClick = { book ->
            val bundle = Bundle()
            bundle.putStringArray("authors", book.author.map { it.name }.toTypedArray() )
            bundle.putString("book", book.title )
            bundle.putString("genre", book.genre )
            findNavController().navigate(R.id.action_SearchFragment_to_DetailsFragment, bundle)
        }

        // TODO so far we just search by genre
        binding.buttonSearch.setOnClickListener {
            val inputManager = this.context?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            inputManager?.hideSoftInputFromWindow(view.windowToken, 0)
            listOfBooks = searchForBook("${searchBox.text}")
            booksListAdapter.books = listOfBooks
            booksListAdapter.notifyDataSetChanged()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("searched_books", Json.encodeToString(listOfBooks))
    }

    private fun searchForBook(requestParams: String) : List<Book> {
        var books: List<Book>
        runBlocking {
            books = apiService.getBooks(requestParams)

            // Seems like always returns 12 books, checked in browser api same stuff
            this@SearchFragment.view?.let {
                Snackbar.make(it, "Found ${books.size} Books of genre $requestParams", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
            }
        }
        return books
    }
}