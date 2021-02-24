package com.francis.mapapplication.model

/**
 * Pokemon data.
 */
data class PokemonData(
    val id: Int,
    val name: String,
    val abilities: List<Abilities>,
    val height: Int,
    val weight: Int,
    val sprites: Sprites,
    val moves: List<Moves>,
    val types: List<Types>,
    val stats: List<Stats>
)

data class Abilities(
    val ability: Ability
)

data class Stats(
    val base_stat: Int,
    val stat: Stat
)

data class Stat(
    val name: String
)

data class Ability(
    val name: String
)

data class Types(
    val type: Type
)

data class Type(
    val name: String
)

data class Sprites(
    val back_default: String?,
    val back_female: String?,
    val back_shiny: String?,
    val front_default: String?,
    val front_female: String?,
    val front_shiny: String?,
    val front_shiny_female: String?
)

data class Moves(
    val move: Move
)

data class Move(
    val name: String
)