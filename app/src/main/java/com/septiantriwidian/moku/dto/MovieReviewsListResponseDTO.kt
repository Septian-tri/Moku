package com.septiantriwidian.moku.dto

data class MovieReviewsListResponseDTO(
    val id : Long,
    val page : Long,
    val results : ArrayList<SingleMovieReviewDetailResponsesDTO>,
    val total_pages : Long,
    val total_results : Long
)
