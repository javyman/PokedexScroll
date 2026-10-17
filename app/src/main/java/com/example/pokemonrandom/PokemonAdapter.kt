package com.example.pokemonrandom

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class PokemonAdapter(
    private var pokemonList: List<PokemonListItem> = emptyList()


) : RecyclerView.Adapter<PokemonAdapter.PokemonViewHolder>() {

    // Cache to prevent re-fetching details when scrolling back and forth
    private val typeCache = mutableMapOf<String, String>()

    fun updateData(newList: List<PokemonListItem>) {
        pokemonList = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PokemonViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pokemon, parent, false)
        return PokemonViewHolder(view, typeCache)
    }

    override fun onBindViewHolder(holder: PokemonViewHolder, position: Int) {

        holder.bind(pokemonList[position])


    }

    override fun getItemCount(): Int = pokemonList.size

    class PokemonViewHolder(
        itemView: View,
        private val typeCache: MutableMap<String, String>
    ) : RecyclerView.ViewHolder(itemView) {
        private val ivPokemon: ImageView = itemView.findViewById(R.id.ivPokemon)
        private val tvNumber: TextView = itemView.findViewById(R.id.tvPokemonNumber)
        private val tvName: TextView = itemView.findViewById(R.id.tvPokemonName)
        private val tvTypes: TextView = itemView.findViewById(R.id.tvPokemonTypes)

        private var fetchJob: Job? = null

        fun bind(pokemon: PokemonListItem) {
            fetchJob?.cancel()

            tvName.text = pokemon.name.replaceFirstChar { it.uppercase() }
            tvNumber.text = "#${pokemon.id.toString().padStart(3, '0')}"

            ivPokemon.load(pokemon.imageUrl) {
                crossfade(true)
                placeholder(android.R.drawable.ic_menu_gallery)
                error(android.R.drawable.ic_dialog_alert)
            }

            val cachedTypes = typeCache[pokemon.name]
            if (cachedTypes != null) {
                tvTypes.text = "Type: $cachedTypes"
            } else {
                tvTypes.text = "Type: Loading..."
                fetchJob = CoroutineScope(Dispatchers.Main).launch {
                    try {
                        val details = PokeApiService.api.getPokemonDetails(pokemon.name)
                        val typesFormatted = details.types.joinToString(", ") {
                            it.type.name.replaceFirstChar { char -> char.uppercase() }
                        }
                        typeCache[pokemon.name] = typesFormatted
                        tvTypes.text = "Type: $typesFormatted"
                    } catch (e: Exception) {
                        tvTypes.text = "Type: Unknown"
                    }
                }
            }
        }
    }
}