package com.septiantriwidian.moku.dto

data class MoviesListResponseDTO(
    var page : Long,
    var results: ArrayList<SingleMovieResponseDTO>,
    var total_pages : Long,
    var total_results : Long
)