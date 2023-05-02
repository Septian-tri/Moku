package com.septiantriwidian.moku.utils

import android.graphics.BitmapFactory
import java.io.BufferedInputStream

class ScaledImages() {

    fun calculateInSampleSize(bufferedInputStream: BufferedInputStream, reqWidth: Int, reqHeight : Int) : Int {

        val width : Int
        val height : Int

        BitmapFactory.Options().run {
            inJustDecodeBounds = true
            BitmapFactory.decodeStream(bufferedInputStream, null, this)
            inJustDecodeBounds = false

            height = this.outHeight
            width  = this.outWidth
        }

        println("$height, $width || $reqHeight, $reqWidth")
        var inSampleSize = 1

        if(height > reqHeight || width > reqWidth){
            val halfHeight: Int = height/2
            val halfWidth: Int = width/2

            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth){
                inSampleSize *= 2
            }

        }
        return inSampleSize
    }

}