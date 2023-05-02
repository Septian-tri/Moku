package com.septiantriwidian.moku.dto

import java.sql.Timestamp

data class SingleMovieReviewDetailResponsesDTO(
    val author : String,
    val author_details : SingleMovieReviewAuthorResponseDTO,
    val content: String,
    val created_at : Timestamp,
    val id : String,
    val updated_at : Timestamp,
    val url : String
)
