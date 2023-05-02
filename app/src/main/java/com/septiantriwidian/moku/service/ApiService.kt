package com.septiantriwidian.moku.service

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.google.gson.Gson
import com.septiantriwidian.moku.dto.*
import com.septiantriwidian.moku.utils.constant.ApiUrl
import com.septiantriwidian.moku.utils.constant.MoviesTrendingMedia
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.BufferedInputStream
import java.io.IOException

class ApiService constructor(private val context : Context, language : String){

    private var requestLanguage : String
    private var networkService : NetworkService
    private var failedImageAlt : Bitmap
    var baseUri : String?
    var trendingMedia : MoviesTrendingMedia? = null

    init {
        this.failedImageAlt = BitmapFactory.decodeStream(context.assets.open("failed_download.png"))
        this.networkService = NetworkService()
        this.baseUri = null
        this.requestLanguage = ""

        if(language != null){
            this.requestLanguage = ApiUrl.MOVIE_LANG_PARAM + language
        }

        if(trendingMedia == null){
            this.trendingMedia = MoviesTrendingMedia.all
        }

    }

    fun fetchTrendingMovies(resultCallback: (result : ArrayList<SingleMovieResponseDTO>) -> Unit) {

        val uri = baseUri?:String.format(ApiUrl.MOVIE_TRENDING_ENP + requestLanguage, trendingMedia)

        networkService.headerHost = ApiUrl.MOVIE_HOST
        networkService.GET(uri.toString(), object  : Callback{

            override fun onFailure(call: Call, e: IOException) {
                println(e.localizedMessage)
            }

            override fun onResponse(call: Call, response: Response) {

                if(!response.isSuccessful){
                    println("Data gagal di muat, errcode : " + response.code())
                }else{

                    try {
                        val bodyResponse = response.body()?.string()
                        val gson = Gson()
                        val parseResponse = gson.fromJson(bodyResponse, MoviesListResponseDTO::class.java)

                        resultCallback(parseResponse.results)

                        response.close()
                    }catch (e : Exception){
                        println(e.localizedMessage)
                    }

                }

            }


        })

    }

    fun fetchGenresMovie(resultCallback: (result: MovieGenresListResponseDTO) -> Unit){

        val uri = baseUri?:(ApiUrl.MOVIE_LIST_GENRES_ENP + requestLanguage)

        networkService.headerHost = ApiUrl.MOVIE_HOST
        networkService.GET(uri.toString(), object : Callback{
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {

                if(!response.isSuccessful || response.body() == null){
                    throw Exception(response.message().toString())
                }

                val bodyResponse = response.body()?.string()
                val parseJson = Gson().fromJson(bodyResponse, MovieGenresListResponseDTO::class.java)

                resultCallback(parseJson)
                response.close()

            }

        })

    }

    fun fetchMoviesListByGenre(genreId : Long, page : Long, resultCallback : (result : MoviesListResponseDTO) -> Unit){

        val uri = baseUri?:(ApiUrl.MOVIE_LIST_BY_GENRE_ENP +  genreId + ApiUrl.MOVIE_PAGE_PARAM + page + requestLanguage)

        networkService.headerHost = ApiUrl.MOVIE_HOST
        networkService.GET(uri.toString(), object : Callback{

            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                if(!response.isSuccessful || response.body() == null){
                    throw Exception(response.message().toString())
                }

                try {
                    val bodyResponse = response.body()?.string()
                    val parseJson = Gson().fromJson(bodyResponse, MoviesListResponseDTO::class.java)

                    resultCallback(parseJson)
                    response.close()
                }catch (e : Exception){
                    e.printStackTrace()
                }
            }

        })

    }

    fun fetchImage(posterPath: String,  resultImageCallback : (image : Bitmap) -> Unit){

        val uri = baseUri?:(ApiUrl.IMAGE_COVER_ENP + posterPath)

        networkService.headerHost = ApiUrl.IMAGE_HOST
        networkService.GET(uri.toString(), object : Callback {

            override fun onFailure(call: Call, e: IOException) {
                resultImageCallback(failedImageAlt)
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {

                if(response.isSuccessful){
                    val inputStream = response.body()?.byteStream()

                    if( inputStream != null && response.code() == 200){

                        try{

                            val bufferedInputStream = BufferedInputStream(inputStream)
                            val options = BitmapFactory.Options()
                                options.inSampleSize = 2

                            resultImageCallback(BitmapFactory.decodeStream(bufferedInputStream, null, options)!!)

                            response.close()
                            call.cancel()

                        }catch (e : Exception){
                            e.printStackTrace()
                            resultImageCallback(failedImageAlt)
                        }

                    }
                }else{
                    resultImageCallback(failedImageAlt)
                    println(response.message())
                }

            }

        })
    }

    fun fetchMovieTrailer(movieId : Long, trailerResponse : (trailerList : MovieTrailersListResponseDTO) -> Unit){

        val uri = baseUri?:String.format(ApiUrl.MOVIE_TRAILER_ENP, movieId)

        networkService.headerHost = ApiUrl.MOVIE_HOST
        networkService.GET(uri.toString(), object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                println(e.localizedMessage)
            }

            override fun onResponse(call: Call, response: Response) {

                if(response.isSuccessful && response.body() != null){

                    try {

                        val responseBody =  response.body()!!.string()
                        val parseJson = Gson().fromJson(responseBody, MovieTrailersListResponseDTO::class.java)
                        trailerResponse(parseJson)
                        response.close()

                    }catch (e : Exception){
                        e.printStackTrace()
                    }

                }

            }

        })

    }

    fun fetchMovieDetail(movieId: Long, movieDetailResponse : (movieDetail : SingleMovieDetailResponseDTO) -> Unit){

        val uri = baseUri?:String.format(ApiUrl.MOVIE_DETAIL_ENP + requestLanguage, movieId)

        networkService.headerHost = ApiUrl.MOVIE_HOST
        networkService.GET(uri.toString(), object : Callback{
            override fun onFailure(call: Call, e: IOException) {
                println(e.localizedMessage)
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                try{

                    if(response.isSuccessful && response.body() != null){
                        val bodyResponse = response.body()!!.string()
                        val parseJson    = Gson().fromJson(bodyResponse, SingleMovieDetailResponseDTO::class.java)
                        movieDetailResponse(parseJson)
                        response.close()
                    }

                }catch (e : Exception){
                    println(e.localizedMessage)
                    e.printStackTrace()
                }
            }

        })

    }

    fun fetchMovieReviews(movieId: Long, page: Long, movieReviewsResponse : (movieReview : MovieReviewsListResponseDTO) -> Unit){

        val uri = baseUri?:String.format(ApiUrl.MOVIE_REVIEWS_ENP, movieId, page)

        networkService.headerHost = ApiUrl.MOVIE_HOST
        networkService.GET(uri.toString(), object : Callback{

            override fun onFailure(call: Call, e: IOException) {
                println(e.localizedMessage)
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                try {

                    if(response.isSuccessful && response.body() != null){

                        val responseBody  = response.body()!!.string()
                        val parseResponse = Gson().fromJson(responseBody, MovieReviewsListResponseDTO::class.java)
                        movieReviewsResponse(parseResponse)
                        response.close()

                    }

                }catch (e : Exception){
                    println(e.localizedMessage)
                    e.printStackTrace()
                }

            }
        })

    }

    fun fetchAvatarImage(avatarPath : String?, avatarImageResult: (avatarImage : Bitmap) -> Unit){

        if(avatarPath != null){

            var avatarEnp : String

            if (avatarPath.contains("https|http".toRegex())){
                networkService.headerHost = ApiUrl.AVATAR_IMAGE_HOST
                avatarEnp = avatarPath
            } else {
                networkService.headerHost = ApiUrl.IMAGE_HOST
                avatarEnp = ApiUrl.AVATAR_IMAGE_ENP + avatarPath
            }

            val uri = baseUri?:avatarEnp.trimStart('/')

            networkService.GET(uri.toString(), object : Callback {

                    override fun onFailure(call: Call, e: IOException) {
                        avatarImageResult(failedImageAlt)
                        e.printStackTrace()
                    }

                    override fun onResponse(call: Call, response: Response) {

                        if(response.isSuccessful){
                            val inputStream = response.body()?.byteStream()

                            if( inputStream != null && response.code() == 200){

                                try{

                                    val bufferedInputStream = BufferedInputStream(inputStream)
                                    val options = BitmapFactory.Options()
                                    options.inSampleSize = 2

                                    avatarImageResult(BitmapFactory.decodeStream(bufferedInputStream, null, options)!!)

                                    response.close()
                                    call.cancel()

                                }catch (e : Exception){
                                    e.printStackTrace()
                                    avatarImageResult(failedImageAlt)
                                }

                            }
                        }else{
                            avatarImageResult(failedImageAlt)
                            println(response.message())
                        }

                    }

                })

        }else{
            avatarImageResult(failedImageAlt)
        }

    }

}