package com.example.myapi.network

import com.example.myapi.model.Post
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiService {

    @GET("posts")
    suspend fun buscarTodos(): List<Post>

    @GET("posts/{id}")
    suspend fun buscarPostPorId(
        @Path("id") postId: Int
    ): Post

    @POST("posts")
    suspend fun salvar(
        @Body novoPost: Post
    ): Post

    @PUT("posts/{id}")
    suspend fun atualizar(
        @Path("id") id: Int,
        @Body post: Post
    ): Post

    @DELETE("posts/{id}")
    suspend fun deletar(
        @Path("id") id: Int
    )
}
