package com.septiantriwidian.moku.dto

import android.graphics.Bitmap
import java.io.Serializable

data class SingleMovieResponseDTO(
    val adult: Boolean,
    val backdrop_path: String,
    val id: Long,
    val title: String,
    val name: String,
    val original_language: String,
    val original_title: String,
    val overview: String,
    val poster_path: String,
    val media_type: String,
    val genre_ids: ArrayList<Long>,
    val popularity: Double,
    val release_date: String,
    val video: Boolean,
    val vote_average: Double,
    val vote_count: Long,
    var imageCover : Bitmap?
) : Serializable