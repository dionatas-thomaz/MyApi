package com.example.myapi.model

import com.google.gson.annotations.SerializedName

data class Post(
    val userId: Int,
    val id: Int? = null,
    val title: String,
    @SerializedName("body")
    val text: String
)
