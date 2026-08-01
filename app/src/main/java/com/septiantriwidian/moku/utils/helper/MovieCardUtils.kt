package com.septiantriwidian.moku.utils.helper

import android.app.Activity
import android.graphics.Point
import com.septiantriwidian.moku.dto.MovieCardUtilsDTO
import kotlin.math.floor

class MovieCardUtils(activity:Activity) {
    private var screenWidth : Int
    private var screenHeight : Int
    private val movieCardPercentage : Double = 30.0

    init {
        val getDisplayResolution = activity.windowManager.defaultDisplay
        val displayResolutionPoint = Point()

        getDisplayResolution.getSize(displayResolutionPoint)
        screenWidth = displayResolutionPoint.x
        screenHeight = displayResolutionPoint.y
    }

    fun details() : MovieCardUtilsDTO {
        val movieCardWidth = floor((screenWidth / 100) * movieCardPercentage).toInt()
        val calcCardPerRow = floor((screenWidth / movieCardWidth).toDouble()).toInt()
        val calcAvailableScreenLeft = screenWidth - (movieCardWidth * calcCardPerRow)
        val movieCardHeight = movieCardWidth + ((movieCardWidth / 100) * movieCardPercentage).toInt()
        val calcMargin = (calcAvailableScreenLeft / calcCardPerRow) / 2
        val movieCardUtilsDTO = MovieCardUtilsDTO()

        movieCardUtilsDTO.setCardMargin(calcMargin)
        movieCardUtilsDTO.setCardWidth(movieCardWidth)
        movieCardUtilsDTO.setCardHeight(movieCardHeight)
        movieCardUtilsDTO.setScreenWidth(this.screenWidth)
        movieCardUtilsDTO.setScreenHeight(this.screenHeight)

        return movieCardUtilsDTO
    }
}