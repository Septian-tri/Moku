package com.septiantriwidian.moku.dto

import java.io.Serializable

data class SingleMovieDetailResponseDTO(
    val budget : Long,
    val genres : ArrayList<SingleMovieGenreResponseDTO>,
    val homepage : String,
    val imdb_id : String,
    val revenue : Long,
    val runtime : Int,
    val status : String,
    val tagline : String
) : Serializable
