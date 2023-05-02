package com.septiantriwidian.moku.dto

data class MovieTrailersListResponseDTO(
    var id : Long,
    var results : ArrayList<SingleMovieTrailerResponseDTO>
)
