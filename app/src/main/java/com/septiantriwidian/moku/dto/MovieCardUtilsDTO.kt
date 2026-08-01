package com.septiantriwidian.moku.dto

class MovieCardUtilsDTO {
    private var cardWidth : Int = 0
    private var cardHeight : Int = 0
    private var cardMargin : Int = 0
    private var screenWidth : Int = 0
    private var screenHeight : Int = 0

    fun getCardWidth() : Int = cardWidth
    fun getCardHeight() : Int = cardHeight
    fun getCardMargin() : Int = cardMargin
    fun getScreenWidth() : Int = screenWidth
    fun getScreenHeight() : Int = screenHeight
    fun setCardWidth(cardWidth: Int) {this.cardWidth=cardWidth}
    fun setCardMargin(cardMargin: Int) {this.cardMargin=cardMargin}
    fun setCardHeight(cardHeight: Int) {this.cardHeight=cardHeight}
    fun setScreenWidth(screenWidth:Int) {this.screenWidth = screenWidth}
    fun setScreenHeight(screenHeight: Int) {this.screenHeight = screenHeight}
}