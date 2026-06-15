package com.example.myapi.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapi.R
import com.example.myapi.model.Post

class PostAdapter(
    private val onItemClick: (Post) -> Unit
) : RecyclerView.Adapter<PostAdapter.ViewHolder>() {

    private var posts = emptyList<Post>()

    fun setPosts(posts: List<Post>) {
        this.posts = posts
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_post, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(posts[position])
    }

    override fun getItemCount() = posts.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textViewUserId: TextView = itemView.findViewById(R.id.textViewUserId)
        private val textViewTitle: TextView = itemView.findViewById(R.id.textViewTitle)
        private val textViewBody: TextView = itemView.findViewById(R.id.textViewBody)

        fun bind(post: Post) {
            textViewUserId.text = "User #${post.userId}"
            textViewTitle.text = post.title
            textViewBody.text = post.text
            itemView.setOnClickListener { onItemClick(post) }
        }
    }
}
