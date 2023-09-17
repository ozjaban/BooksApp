package com.example.booksapp

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentTransaction
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.booksapp.databinding.FragmentFavoritesBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.FileReader
import java.io.FileWriter

/**
 * Here we gonna take all that are favorited aka saved on device
 */
class FavoritesFragment : Fragment() {
    private var _binding: FragmentFavoritesBinding? = null
    private val favoriteBooksList = mutableListOf<Book>()
    private lateinit var favoritesFileFullPath: String
    private val favoritesFileName = "favorites"

    private val binding get() = _binding!!

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFavoritesBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        favoritesFileFullPath = ContextCompat.getExternalFilesDirs(this.requireContext(), null)[0].absolutePath + File.separator + "/favDir"
        favoriteBooksList.getSavedBooks()

        val booksListAdapter = if (favoriteBooksList.isEmpty()) {
            BooksListAdapter(listOf(Book(title = "NO FAVORITES")), this)
        } else {
            BooksListAdapter(favoriteBooksList, this)
        }

        val recyclerView = view.findViewById<RecyclerView>(R.id.rv_favorites_list)
        recyclerView?.layoutManager = LinearLayoutManager(context)
        recyclerView?.adapter = booksListAdapter

        booksListAdapter.onItemClick = { book ->
            val bundle = Bundle()
            bundle.putStringArray("authors", book.author.map { it.name }.toTypedArray() )
            bundle.putString("book", book.title )
            bundle.putString("genre", book.genre )
            findNavController().navigate(R.id.action_FavoritesFragment_to_DetailsFragment, bundle)
        }

        binding.buttonFirst.setOnClickListener {
            findNavController().navigate(R.id.action_FavoritesFragment_to_SearchFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        favoriteBooksList.getSavedBooks()
    }

    private fun MutableList<Book>.getSavedBooks()  {
        this@getSavedBooks.clear()
        CoroutineScope(Dispatchers.IO).run {
            val favoritesFile = File(favoritesFileFullPath + File.separator + favoritesFileName)
            try {
                val bufferedReader = BufferedReader(FileReader(favoritesFile))

                for (line in bufferedReader.lines()) {
                    val book = line.split("#")
                    val author = book[1].filterNot { it == '[' || it == ']' }
                    this@getSavedBooks.add(Book(mutableListOf(Author(name = author)), book[0]))
                }
                bufferedReader.close()

            } catch (ex: Exception) {
                Log.e("Failed to write to file", "${ex.message}")
            }
        }
    }
}