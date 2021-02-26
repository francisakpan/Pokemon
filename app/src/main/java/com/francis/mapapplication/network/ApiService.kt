package com.francis.mapapplication.network

import com.francis.mapapplication.model.PokemonData
import com.francis.mapapplication.model.Property
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import io.reactivex.rxjava3.core.Observable
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

// pokeapi base url
private const val BASE_URL = "https://pokeapi.co/api/v2/"

// moshi instance
private val moshi = Moshi
    .Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

// pokeapi retrofit instance.
private val retrofit = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
    .baseUrl(BASE_URL)
    .build()

// Interface to query the pokeapi endpoints.
interface ApiService{
    @GET("pokemon")
    fun getProperties(
        @Query("offset") offset: Int, @Query("limit") limit: Int): Observable<Property>

    @GET("pokemon/{id}/")
    fun getPokemon(@Path("id") id: String): Observable<PokemonData>
}

//singleton pokeapi instance for making network calls.
object PokemonApi {
    val retrofitService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}