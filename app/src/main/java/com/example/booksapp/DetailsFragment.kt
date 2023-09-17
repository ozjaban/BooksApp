package com.example.booksapp

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.booksapp.databinding.FragmentDetailsBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import java.io.BufferedWriter
import java.io.File
import java.io.File.separator
import java.io.FileWriter

/**
 *
 */
class DetailsFragment : Fragment() {
    private var _binding: FragmentDetailsBinding? = null
    private var fab: FloatingActionButton? = null
    private lateinit var favoritesFileFullPath: String
    private val favoritesFileName = "favorites"

    private val binding get() = _binding!!

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentDetailsBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // TODO for some reason not displaying shit when navigating from other fragments
        val detailsBook = view.findViewById<TextView>(R.id.details_book)
        val detailsAuthors = view.findViewById<TextView>(R.id.details_authors)
        val bookName = arguments?.getString("book")
        val authors = arguments?.getStringArray("authors")
        detailsBook.text = bookName
        val authorsList = mutableListOf<Author>()
        var authorsString = ""
        authors?.forEach {
            authorsList.add(Author("default", it))
            authorsString += "$it "
        }
        detailsAuthors.text = authorsString

        fab = activity?.findViewById(R.id.fab)
        fab?.visibility = View.VISIBLE

        favoritesFileFullPath = ContextCompat.getExternalFilesDirs(this.requireContext(), null)[0].absolutePath + separator + "/favDir"
        val fileParentDir = File(favoritesFileFullPath)
        if (!fileParentDir.exists()) {
            fileParentDir.mkdir()
        }
        fab?.setOnClickListener {
            val wasAdded = updateFavoriteBooks(Book(authorsList, bookName!!), this.requireContext())
            val action = if (wasAdded) "Added to" else "Deleted from"
            Snackbar.make(view, "Book $bookName was $action favorites", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onPause() {
        super.onPause()
        fab?.visibility = View.GONE
    }

    override fun onResume() {
        super.onResume()
        fab?.visibility = View.VISIBLE
    }

    private fun updateFavoriteBooks(book: Book, context: Context): Boolean {
        var wasAdded = true
        val favoritesFile = File(favoritesFileFullPath + separator + favoritesFileName)
        val tmp = File(favoritesFileFullPath + separator + "${favoritesFileName}tmp")
        val authors = book.author.map { it.name }
        try {
            // Fuck it just go through lines and create new file, so we can delete as well
            if (!favoritesFile.exists() || favoritesFile.length() == 0L) {
                favoritesFile.createNewFile()
                val bw = BufferedWriter(FileWriter(favoritesFile))
                bw.write("${book.title}#$authors")
                bw.newLine()
                bw.close()
            } else {
                tmp.createNewFile()
                val bufferedWriter = BufferedWriter(FileWriter(tmp))
                favoritesFile.bufferedReader().use {
                    for (line in it.readLines()) {
                        if (line.trim() == "${book.title}#$authors") {
                            wasAdded = false
                            continue
                        }
                        bufferedWriter.write(line)
                        bufferedWriter.newLine()
                    }
                }
                if (wasAdded) {
                    bufferedWriter.write("${book.title}#$authors")
                    bufferedWriter.newLine()
                }

                tmp.renameTo(favoritesFile)
                bufferedWriter.close()
            }
        } catch (ex: Exception) {
            Log.e("Failed to write to file", "${ex.message}")
        }
        return wasAdded
    }
}