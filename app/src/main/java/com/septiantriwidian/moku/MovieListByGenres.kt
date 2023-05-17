package com.septiantriwidian.moku

import android.content.Intent
import android.graphics.Point
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.StrictMode
import android.view.LayoutInflater
import android.view.View
import android.view.ViewTreeObserver
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.LinearLayout.LayoutParams
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import androidx.core.view.setMargins
import com.septiantriwidian.moku.service.ApiService
import com.septiantriwidian.moku.utils.constant.ViewCardMoviesSetting
import kotlinx.coroutines.Runnable
import kotlin.math.ceil

class MovieListByGenres : AppCompatActivity() {

    lateinit var apiService : ApiService
    var cardViewLyParam : LayoutParams?
    var scrollLayoutParent: LinearLayout?
    var bufferAnimate : View?
    var calcCardViewMargin : Int
    var cardViewHeight : Int
    var cardViewWidth : Int
    var viewCardPerLine : Int
    var movieStartPage: Long
    var totalMoviePage: Long
    var loadNewPage : Boolean

    init{
        this.cardViewHeight = 0
        this.cardViewWidth = 0
        this.totalMoviePage = 0
        this.viewCardPerLine = 0
        this.calcCardViewMargin = 0
        this.movieStartPage = 1
        this.loadNewPage = false
        this.bufferAnimate = null
        this.scrollLayoutParent = null
        this.cardViewLyParam = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val threadPolicy   = StrictMode.ThreadPolicy.Builder().permitAll().build()
        val fullScreenFlag = WindowManager.LayoutParams.FLAG_FULLSCREEN
        val intent         = intent.extras
        val genreId        = intent!!.getLong("genreId")
        val genreName      = intent.getString("genreName")
        val parentScroll : ScrollView = findViewById(R.id.parentScrollView)
        val getDisplayResolution = windowManager.defaultDisplay
        val displayResolutionPoint = Point()
            getDisplayResolution.getSize(displayResolutionPoint)
        val screenWidth = displayResolutionPoint.x
            apiService = ApiService(applicationContext, "id")

            super.setTitle(genreName!!.uppercase())
            window.setFlags(fullScreenFlag, fullScreenFlag)
            setContentView(R.layout.activity_movie_list_by_genre)
            StrictMode.setThreadPolicy(threadPolicy)

            //setup the grid view card for list movies
            scrollLayoutParent   = findViewById(R.id.parentLayoutMovieListByGenre)
            cardViewHeight       = ViewCardMoviesSetting.MOVIE_CARDVIEW_HEIGHT
            cardViewWidth        = ViewCardMoviesSetting.MOVIE_CARDVIEW_WIDTH
            viewCardPerLine      = ceil((screenWidth/cardViewWidth).toDouble()).toInt()
            calcCardViewMargin   = ((screenWidth - (viewCardPerLine*cardViewWidth)) / viewCardPerLine) / 2
            cardViewLyParam      = LayoutParams(cardViewWidth, cardViewHeight)
            movieStartPage       = 1
            totalMoviePage       = 0
            loadNewPage          = false
            cardViewLyParam!!.setMargins(calcCardViewMargin)


            //giving  buffer progressbar animation for first time loaded page
            bufferAnimate = LayoutInflater.from(applicationContext).inflate(R.layout.buffer_progressbar_animate, null)
            scrollLayoutParent!!.addView(bufferAnimate)


        //do endless scroll and load the movies lists
        val handler = Handler(Looper.getMainLooper())
            parentScroll.viewTreeObserver.addOnScrollChangedListener(object : ViewTreeObserver.OnScrollChangedListener{
                override fun onScrollChanged() {

                    fun checkNextPage() : Boolean{
                        if(loadNewPage == true && totalMoviePage > 1 && movieStartPage <= totalMoviePage){
                            loadNewPage = false
                            movieStartPage++//adding buffer animate before load next page
                            loadMovie(genreId)
                            return true
                        }

                        return false
                    }

                    //next page automatically if progressbar animation rendered has parent
                    fun checkRepeatedLoadNextPage(){
                        handler.postDelayed(object : Runnable{
                            override fun run() {

                                if(checkNextPage()){
                                    handler.removeCallbacks(this)
                                }else{
                                    checkRepeatedLoadNextPage()
                                }

                            }

                        }, 1000)
                    }

                    if(parentScroll.scrollY >= (parentScroll.layoutParams.height-cardViewHeight)){

                        checkNextPage()

                        if(bufferAnimate!!.parent == null){
                            scrollLayoutParent!!.addView(bufferAnimate)
                            checkRepeatedLoadNextPage()
                        }

                    }

                }
            })
            loadMovie(genreId)
    }

    fun loadMovie(genreId : Long) {
        apiService.fetchMoviesListByGenre(genreId, movieStartPage) { result ->

            this@MovieListByGenres.runOnUiThread(object : Runnable {

                override fun run() {

                    var totalImageLoad = 0
                    var totalCardRender = viewCardPerLine
                    val totalMovies     = result.results.size

                    var scrollLayoutHorizontal = LinearLayout(applicationContext)
                        scrollLayoutHorizontal.orientation = LinearLayout.HORIZONTAL

                    //removing progressbar animation
                    scrollLayoutParent!!.removeView(bufferAnimate)

                    for (i in 0..(totalMovies- 1)) {

                        val singleMovie = result.results.get(i)
                        val movieName: String = singleMovie.name ?: singleMovie.title
                        val viewCardInflater = LayoutInflater.from(applicationContext).inflate(R.layout.movie_single_cardview, null)
                        val containerTextBottom : LinearLayout = viewCardInflater.findViewById(R.id.singleMovieRootBottomTextContainer)
                        val cardView : FrameLayout      = viewCardInflater.findViewById(R.id.SingleMovieCardView)
                        val titleMovie : TextView       = viewCardInflater.findViewById(R.id.singleMovieCardTitle)
                        val ratingMovie : TextView      = viewCardInflater.findViewById(R.id.singleMovieCardRatingText)
                        val coverMovie : ImageView      = viewCardInflater.findViewById(R.id.singleMovieCardCover)

                        viewCardInflater.layoutParams = cardViewLyParam
                        titleMovie.text = movieName
                        titleMovie.visibility = TextView.GONE
                        ratingMovie.text = String.format("%.1f/10", singleMovie.vote_average)
                        containerTextBottom.visibility = LinearLayout.GONE

                        scrollLayoutHorizontal.addView(viewCardInflater)

                        if (i > totalCardRender){
                            scrollLayoutParent!!.addView(scrollLayoutHorizontal)
                            scrollLayoutHorizontal = LinearLayout(applicationContext) //resetting scroll layout horizontal
                            totalCardRender += viewCardPerLine
                        }

                        apiService.fetchImage(if(singleMovie.poster_path == null) "none" else singleMovie.poster_path) { result ->
                            this@MovieListByGenres.runOnUiThread(object : Runnable {
                                override fun run() {

                                    totalImageLoad++

                                    containerTextBottom.visibility = LinearLayout.VISIBLE
                                    titleMovie.visibility = TextView.VISIBLE
                                    coverMovie.setImageBitmap(result)
                                    cardView.removeView(viewCardInflater.findViewById<ProgressBar>(R.id.singleMovieCardProgressBar))

                                    cardView.setOnClickListener {
                                        val singleMovieDetail = Intent(applicationContext, SingleMovieDetailActivity().javaClass)
                                        singleMovieDetail.putExtra("singleMovie", singleMovie)
                                        startActivity(singleMovieDetail)
                                    }

                                    if(totalImageLoad >= totalMovies){ //waiting for all image cover loaded, and load next page wil be allowed
                                        totalImageLoad = 0
                                        loadNewPage = true
                                    }

                                }

                            })

                        }

                    }

                    totalMoviePage = result.total_pages

                }

            })

        }

    }

}