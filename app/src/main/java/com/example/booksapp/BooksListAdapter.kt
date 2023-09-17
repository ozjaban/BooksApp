package com.example.booksapp

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar

class BooksListAdapter(var books: List<Book>, private val parentFragment: Fragment) : RecyclerView.Adapter<BooksListAdapter.ViewHolder>() {
    var onItemClick: ((Book) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.book_view, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var authors = ""
        books[position].author.forEach {
            authors += it.name + " "
        }
        holder.author.text = authors
        holder.title.text = books[position].title
        holder.itemView.setOnClickListener {
            onItemClick?.let {
                it.invoke(books[position])
            }
        }
    }

    override fun getItemCount(): Int {
       return books.size
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val author = view.findViewById<TextView>(R.id.author)
        val title = view.findViewById<TextView>(R.id.title)
    }
}