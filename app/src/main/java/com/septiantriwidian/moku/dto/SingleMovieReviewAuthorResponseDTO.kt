package com.septiantriwidian.moku.dto

import android.graphics.Bitmap

data class SingleMovieReviewAuthorResponseDTO(
    val name : String,
    val username : String,
    val avatar_path : String,
    val rating : Double,
    var avatar_image : Bitmap
)
