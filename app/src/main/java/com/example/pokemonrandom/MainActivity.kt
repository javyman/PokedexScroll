package com.example.pokemonrandom

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

// Main Pokémon response object
data class PokemonResponse(
    val id: Int,
    val name: String,
    val types: List<TypeSlot>,
    val stats: List<StatSlot>,
    val sprites: Sprites
)

// Types representation (e.g., Electric, Fire)
data class TypeSlot(
    val slot: Int,
    val type: TypeInfo
)

data class TypeInfo(
    val name: String
)

// Base Stats representation (e.g., HP: 35, Attack: 55)
data class StatSlot(
    @SerializedName("base_stat") val baseStat: Int,
    val stat: StatInfo
)

data class StatInfo(
    val name: String
)

// Image URL representation
data class Sprites(
    @SerializedName("front_default") val frontDefault: String?
)

// PokeApiService interface for Retrofit API calls
interface PokeApiService {
    @GET("pokemon/{name}")
    suspend fun getPokemon(@Path("name") name: String): PokemonResponse

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

sealed interface PokemonUiState {
    object Idle : PokemonUiState
    object Loading : PokemonUiState
    data class Success(val pokemon: PokemonResponse) : PokemonUiState
    data class Error(val message: String) : PokemonUiState
}

class PokemonViewModel : ViewModel() {
    var uiState: PokemonUiState by mutableStateOf(PokemonUiState.Idle)
        private set

    fun fetchPokemon(name: String) {
        if (name.isBlank()) return

        viewModelScope.launch {
            uiState = PokemonUiState.Loading
            try {
                val result = PokeApiService.api.getPokemon(name.lowercase().trim())
                uiState = PokemonUiState.Success(result)
            } catch (e: Exception) {
                uiState = PokemonUiState.Error("Pokémon not found or network error!")
            }
        }
    }
}

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PokemonScreen()
        }
    }
}

@Composable
fun PokemonScreen(viewModel: PokemonViewModel = viewModel()) {
    var searchQuery by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Enter Pokémon name (e.g. pikachu)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { viewModel.fetchPokemon(searchQuery) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Search")
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Display results based on UI State
        when (val state = viewModel.uiState) {
            is PokemonUiState.Idle -> Text("Type a Pokémon name to search.")
            is PokemonUiState.Loading -> CircularProgressIndicator()
            is PokemonUiState.Error -> Text(text = state.message, color = MaterialTheme.colorScheme.error)
            is PokemonUiState.Success -> PokemonDetailCard(state.pokemon)
        }
    }
}

@Composable
fun PokemonDetailCard(pokemon: PokemonResponse) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Pokémon Sprite Image
            AsyncImage(
                model = pokemon.sprites.frontDefault,
                contentDescription = pokemon.name,
                modifier = Modifier.size(120.dp)
            )

            // Piece #1: Name and ID
            Text(
                text = "#${pokemon.id} ${pokemon.name.replaceFirstChar { it.uppercase() }}",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Piece #2: Types
            Text(
                text = "Types: " + pokemon.types.joinToString { it.type.name.replaceFirstChar { char -> char.uppercase() } },
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Piece #3: Stats
            Text(text = "Base Stats:", style = MaterialTheme.typography.titleMedium)
            Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                pokemon.stats.forEach { statSlot ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = statSlot.stat.name.uppercase())
                        Text(text = statSlot.baseStat.toString())
                    }
                }
            }
        }
    }
}
