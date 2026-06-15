package com.example.myapi

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapi.model.Post
import com.example.myapi.network.RetrofitClient
import com.example.myapi.ui.DetalhesPostActivity
import com.example.myapi.ui.PostAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PostAdapter
    private val api = RetrofitClient.api
    private val posts = mutableListOf<Post>()
    private var nextId = 101

    private val detalhesLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode != RESULT_OK) return@registerForActivityResult

        val data = result.data ?: return@registerForActivityResult
        val operation = data.getStringExtra("operation")
        val postId = data.getIntExtra("post_id", -1)
        val title = data.getStringExtra("post_title") ?: ""
        val body = data.getStringExtra("post_body") ?: ""

        when (operation) {
            "create" -> {
                val newPost = Post(userId = 1, id = nextId++, title = title, text = body)
                posts.add(0, newPost)
                adapter.setPosts(posts)
                Log.d("API", "Post #${newPost.id} adicionado localmente")
            }
            "update" -> {
                val index = posts.indexOfFirst { it.id == postId }
                if (index != -1) {
                    posts[index] = posts[index].copy(title = title, text = body)
                    adapter.setPosts(posts)
                    Log.d("API", "Post #$postId atualizado localmente")
                }
            }
            "delete" -> {
                posts.removeAll { it.id == postId }
                adapter.setPosts(posts)
                Log.d("API", "Post #$postId removido localmente")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(findViewById(R.id.toolbar))

        recyclerView = findViewById(R.id.recyclerViewPosts)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = PostAdapter { post ->
            val intent = Intent(this, DetalhesPostActivity::class.java)
            intent.putExtra("post_id", post.id)
            detalhesLauncher.launch(intent)
        }
        recyclerView.adapter = adapter

        findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.fab)
            .setOnClickListener {
                val intent = Intent(this, DetalhesPostActivity::class.java)
                detalhesLauncher.launch(intent)
            }

        carregarPosts()
    }

    private fun carregarPosts() {
        lifecycleScope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    api.buscarTodos()
                }
                posts.clear()
                posts.addAll(result)
                nextId = (posts.maxOfOrNull { it.id ?: 0 } ?: 100) + 1
                adapter.setPosts(posts)
                Log.d("API", "Posts carregados: ${posts.size}, próximo ID: $nextId")
            } catch (e: Exception) {
                Log.e("API", e.message ?: "Erro desconhecido")
                Toast.makeText(this@MainActivity, "Erro ao carregar posts", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
