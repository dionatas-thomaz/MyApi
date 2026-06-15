package com.example.myapi.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.myapi.R
import com.example.myapi.model.Post
import com.example.myapi.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DetalhesPostActivity : AppCompatActivity() {

    private lateinit var editTitle: EditText
    private lateinit var editBody: EditText
    private lateinit var buttonSave: Button
    private lateinit var buttonDelete: Button
    private val api = RetrofitClient.api

    companion object {
        private const val TAG = "API"
    }

    private var postId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalhes_post)

        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        editTitle = findViewById(R.id.editTitle)
        editBody = findViewById(R.id.editBody)
        buttonSave = findViewById(R.id.buttonSave)
        buttonDelete = findViewById(R.id.buttonDelete)

        postId = intent.getIntExtra("post_id", -1).let { if (it == -1) null else it }

        if (postId == null) {
            supportActionBar?.title = "Nova Postagem"
            buttonDelete.isEnabled = false
        } else {
            supportActionBar?.title = "Editar Postagem"
            carregarPost(postId!!)
        }

        buttonSave.setOnClickListener { salvarPost() }
        buttonDelete.setOnClickListener { confirmarExclusao() }
    }

    private fun carregarPost(id: Int) {
        lifecycleScope.launch {
            try {
                val post = withContext(Dispatchers.IO) {
                    api.buscarPostPorId(id)
                }
                editTitle.setText(post.title)
                editBody.setText(post.text)
                Log.d(TAG, "Post carregado: ${post.title}")
            } catch (e: Exception) {
                Log.e(TAG, e.message ?: "Erro desconhecido")
                Toast.makeText(this@DetalhesPostActivity, "Erro ao carregar postagem", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun salvarPost() {
        val title = editTitle.text.toString().trim()
        val body = editBody.text.toString().trim()

        if (title.isEmpty() || body.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val savedPost = withContext(Dispatchers.IO) {
                    if (postId == null) {
                        api.salvar(Post(userId = 1, title = title, text = body))
                    } else {
                        api.atualizar(postId!!, Post(userId = 1, title = title, text = body))
                    }
                }
                Log.d(TAG, if (postId == null) "Postagem criada!" else "Postagem atualizada!")

                val resultIntent = Intent().apply {
                    if (postId == null) {
                        putExtra("operation", "create")
                        putExtra("post_id", savedPost.id ?: 101)
                    } else {
                        putExtra("operation", "update")
                        putExtra("post_id", postId)
                    }
                    putExtra("post_title", title)
                    putExtra("post_body", body)
                }
                setResult(RESULT_OK, resultIntent)

                Toast.makeText(
                    this@DetalhesPostActivity,
                    if (postId == null) "Postagem criada com sucesso!" else "Postagem atualizada com sucesso!",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            } catch (e: Exception) {
                Log.e(TAG, e.message ?: "Erro desconhecido")
                Toast.makeText(this@DetalhesPostActivity, "Erro ao salvar", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun confirmarExclusao() {
        AlertDialog.Builder(this)
            .setTitle("Confirmar Exclusão")
            .setMessage("Tem certeza que deseja excluir esta postagem?")
            .setPositiveButton("Sim") { _, _ -> excluirPost() }
            .setNegativeButton("Não", null)
            .show()
    }

    private fun excluirPost() {
        lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    postId?.let { api.deletar(it) }
                }
                Log.d(TAG, "Postagem excluída!")

                val resultIntent = Intent().apply {
                    putExtra("operation", "delete")
                    putExtra("post_id", postId)
                }
                setResult(RESULT_OK, resultIntent)

                Toast.makeText(this@DetalhesPostActivity, "Postagem excluída com sucesso!", Toast.LENGTH_SHORT).show()
                finish()
            } catch (e: Exception) {
                Log.e(TAG, e.message ?: "Erro desconhecido")
                Toast.makeText(this@DetalhesPostActivity, "Erro ao excluir", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
