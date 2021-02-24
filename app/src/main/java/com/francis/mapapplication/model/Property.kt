package com.francis.mapapplication.model

data class Property(
    val results: List<Pokemon>
)

//Pokemon model
data class Pokemon(
    val name: String,
    val url: String
)