package com.example.pokemonrandom

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

// Pokémon list response models
data class PokemonListResponse(
    val results: List<PokemonListItem>
)

data class PokemonListItem(
    val name: String,
    val url: String
) {
    val id: Int
        get() {
            val parts = url.trimEnd('/').split('/')
            return parts.lastOrNull()?.toIntOrNull() ?: 0
        }

    val imageUrl: String
        get() = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/$id.png"
}

// Pokémon detail models for type information
data class PokemonDetailResponse(
    val id: Int,
    val name: String,
    val types: List<TypeSlot>
)

data class TypeSlot(
    val slot: Int,
    val type: TypeInfo
)

data class TypeInfo(
    val name: String
)

// PokeApiService interface for Retrofit API calls
interface PokeApiService {
    @GET("pokemon")
    suspend fun getPokemonList(
        @Query("limit") limit: Int = 100,
        @Query("offset") offset: Int = 0
    ): PokemonListResponse

    @GET("pokemon/{name}")
    suspend fun getPokemonDetails(
        @Path("name") name: String
    ): PokemonDetailResponse

    companion object {
        private const val BASE_URL = "https://pokeapi.co/api/v2/"

        val api: PokeApiService by lazy {
            Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(PokeApiService::class.java)
        }
    }
}

class MainActivity : AppCompatActivity() {

    private lateinit var rvPokemon: RecyclerView
    private lateinit var progressBar: ProgressBar
    private val pokemonAdapter = PokemonAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        rvPokemon = findViewById(R.id.rvPokemon)
        progressBar = findViewById(R.id.progressBar)

        rvPokemon.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        val snapHelper = PagerSnapHelper()
        snapHelper.attachToRecyclerView(rvPokemon)

        rvPokemon.adapter = pokemonAdapter

        fetchPokemonList()
    }

    private fun fetchPokemonList() {
        progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val response = PokeApiService.api.getPokemonList(limit = 100)
                pokemonAdapter.updateData(response.results)
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Failed to load Pokémon!", Toast.LENGTH_SHORT).show()
            } finally {
                progressBar.visibility = View.GONE
            }
        }
    }
}