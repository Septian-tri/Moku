package com.septiantriwidian.moku

import android.content.Intent
import android.graphics.Point
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.StrictMode
import android.view.LayoutInflater
import android.view.ViewGroup
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
import com.septiantriwidian.moku.dto.MoviesListResponseDTO
import com.septiantriwidian.moku.service.ApiService
import com.septiantriwidian.moku.view.CustomActionBar
import com.septiantriwidian.moku.utils.constant.IntentKey
import com.septiantriwidian.moku.utils.constant.MovieDetailMediaType
import com.septiantriwidian.moku.utils.constant.ViewCardMoviesSetting
import kotlinx.coroutines.Runnable
import java.net.URLEncoder
import kotlin.math.ceil

class MovieListActivity : AppCompatActivity() {

    private lateinit var apiService : ApiService
    private lateinit var scrollLayoutParent: ViewGroup
    private lateinit var bufferAnimate : ProgressBar
    private var cardViewLyParam : LayoutParams?
    private var calcCardViewMargin : Int
    private var cardViewHeight : Int
    private var cardViewWidth : Int
    private var viewCardTotalShowPerRow : Int
    private var movieStartPage: Long
    private var totalMoviePage: Long
    private var loadNewPage : Boolean
    private var screenWidth : Int
    private var screenHeight : Int
    private var genreId : Long
    private var searchQuery : String
    private var mediaMovie : String
    private var rowsFinishRender : Long

    init{
        this.cardViewHeight = 0
        this.cardViewWidth = 0
        this.totalMoviePage = 0
        this.viewCardTotalShowPerRow = 0
        this.calcCardViewMargin = 0
        this.movieStartPage = 1
        this.loadNewPage = false
        this.cardViewLyParam = null
        this.screenWidth = 0
        this.screenHeight = 0
        this.genreId = 0
        this.searchQuery = ""
        this.mediaMovie = ""
        this.rowsFinishRender = 0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_movie_list)

        val intent = intent.extras!!
        apiService = ApiService(applicationContext, "id")
        genreId = intent.getLong(IntentKey.GENRE_ID.name)
        mediaMovie = intent.getString(IntentKey.MEDIA_MOVIE.name) as String
        searchQuery = intent.getString(IntentKey.SEARCH_QUERY.name) as String

        val threadPolicy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        val fullScreenFlag = WindowManager.LayoutParams.FLAG_FULLSCREEN
        val genreName = intent.getString(IntentKey.GENRE_NAME.name) as String
        val parentScroll : ScrollView = findViewById(R.id.parentScrollView)
        val getDisplayResolution = windowManager.defaultDisplay
        val displayResolutionPoint = Point()
        val headerContainer : LinearLayout = findViewById(R.id.movieListHeader);
        val viewTitle = if(mediaMovie == MovieDetailMediaType.BY_ID_GENRE.name)  genreName.uppercase() else searchQuery

        getDisplayResolution.getSize(displayResolutionPoint)
        CustomActionBar(headerContainer, viewTitle, true).inflateHeader()
        StrictMode.setThreadPolicy(threadPolicy)

        this.screenWidth = displayResolutionPoint.x
        this.screenHeight = displayResolutionPoint.y
        window.setFlags(fullScreenFlag, fullScreenFlag)

        //setup the grid view card for list movies
        scrollLayoutParent   = findViewById(R.id.parentLayoutMovieList)
        cardViewHeight       = ViewCardMoviesSetting.MOVIE_CARDVIEW_HEIGHT
        cardViewWidth        = ViewCardMoviesSetting.MOVIE_CARDVIEW_WIDTH
        viewCardTotalShowPerRow      = ceil((screenWidth/cardViewWidth).toDouble()).toInt()
        calcCardViewMargin   = ((screenWidth - (viewCardTotalShowPerRow*cardViewWidth)) / viewCardTotalShowPerRow) / 2
        cardViewLyParam      = LayoutParams(cardViewWidth, cardViewHeight)
        movieStartPage       = 1
        totalMoviePage       = 1
        loadNewPage          = true
        cardViewLyParam!!.setMargins(calcCardViewMargin)

        //giving  buffer progressbar animation for first time loaded page
        bufferAnimate = LayoutInflater.from(applicationContext).inflate(R.layout.buffer_progressbar_animate, null) as ProgressBar


        //do endless scroll and load the movies lists
        val handler = Handler(Looper.getMainLooper())
        parentScroll.viewTreeObserver.addOnScrollChangedListener(object : ViewTreeObserver.OnScrollChangedListener{
            override fun onScrollChanged() {
                //next page automatically if progressbar animation rendered has parent
                fun checkRepeatedLoadNextPage(){
                    handler.postDelayed(object : Runnable{
                        override fun run() {
                            if(loadNewPage){
                                fetchMovie()
                                handler.removeCallbacks(this)
                            }else{
                                checkRepeatedLoadNextPage()
                            }
                        }
                    }, 1000)
                }

                if(parentScroll.scrollY >= (parentScroll.layoutParams.height-cardViewHeight)){
                    fetchMovie()
                    if(bufferAnimate.parent == null){
                        scrollLayoutParent.addView(bufferAnimate)
                        checkRepeatedLoadNextPage()
                    }
                }
            }

        })

        fetchMovie()
        recheckMovieRenderHeight()
    }

    private fun loadMovie(result : MoviesListResponseDTO) {

        this@MovieListActivity.runOnUiThread(Runnable {
            var totalImageLoad = 0
            var viewCardPerRowFinishRender = viewCardTotalShowPerRow //start finish render
            val totalMovies = result.results.size
            var scrollLayoutHorizontal = LinearLayout(applicationContext)


            scrollLayoutHorizontal.orientation = LinearLayout.HORIZONTAL

            //removing progressbar animation
            scrollLayoutParent.removeView(bufferAnimate)

            //looping view card for make grid view
            for (i in 0..(totalMovies-1)) {

                val singleMovie = result.results.get(i)
                val movieName: String = singleMovie.name ?: singleMovie.title
                val viewCardInflater = LayoutInflater.from(applicationContext).inflate(R.layout.movie_single_cardview, null)
                val containerTextBottom : LinearLayout = viewCardInflater.findViewById(R.id.singleMovieRootBottomTextContainer)
                val cardView : FrameLayout = viewCardInflater.findViewById(R.id.SingleMovieCardView)
                val titleMovie : TextView = viewCardInflater.findViewById(R.id.singleMovieCardTitle)
                val ratingMovie : TextView = viewCardInflater.findViewById(R.id.singleMovieCardRatingText)
                val coverMovie : ImageView = viewCardInflater.findViewById(R.id.singleMovieCardCover)

                viewCardInflater.layoutParams = cardViewLyParam
                titleMovie.text = movieName
                titleMovie.visibility = TextView.GONE
                ratingMovie.text = String.format("%.1f/10", singleMovie.vote_average)
                containerTextBottom.visibility = LinearLayout.GONE

                scrollLayoutHorizontal.addView(viewCardInflater)
                if (i > viewCardPerRowFinishRender){
                    rowsFinishRender++
                    scrollLayoutParent.addView(scrollLayoutHorizontal)
                    scrollLayoutHorizontal = LinearLayout(applicationContext) //resetting scroll layout horizontal
                    viewCardPerRowFinishRender += viewCardTotalShowPerRow
                }

                apiService.fetchImage(if(singleMovie.poster_path == null) "none" else singleMovie.poster_path) { result ->
                    this@MovieListActivity.runOnUiThread(Runnable {
                        totalImageLoad++

                        containerTextBottom.visibility = LinearLayout.VISIBLE
                        titleMovie.visibility = TextView.VISIBLE
                        coverMovie.setImageBitmap(result)
                        cardView.removeView(viewCardInflater.findViewById<ProgressBar>(R.id.singleMovieCardProgressBar))

                        cardView.setOnClickListener {
                            val singleMovieDetail = Intent(applicationContext, SingleMovieDetailActivity().javaClass)
                            singleMovieDetail.putExtra("singleMovie", singleMovie)
                            intent.flags = Intent.FLAG_ACTIVITY_NO_HISTORY
                            startActivity(singleMovieDetail)
                        }

                        if(totalImageLoad >= totalMovies){ //waiting for all image cover loaded, and load next page wil be allowed
                            totalImageLoad = 0
                            loadNewPage = true
                        }
                    })

                }

            }

            totalMoviePage = result.total_pages

        })

    }

    private fun fetchMovie(){

        if(loadNewPage && movieStartPage <= totalMoviePage){

            if(bufferAnimate.parent == null) {
                scrollLayoutParent.addView(bufferAnimate)
            }

            loadNewPage = false
            movieStartPage++

            if(mediaMovie == MovieDetailMediaType.BY_ID_GENRE.name){
                apiService.fetchMoviesListByGenre(genreId, movieStartPage) { result ->
                    loadMovie(result)
                }
            }else{
                apiService.fetchMovieSearch(URLEncoder.encode(searchQuery), movieStartPage) { result ->
                    loadMovie(result)
                }
            }
            println("load new page")
        }
    }

    private fun recheckMovieRenderHeight(){

        //recheck the results render of view card if all stack view card
        //cannot overflow the screen height do load movie method again

        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed(object : Runnable {
            override fun run() {
                val calcViewCardHasRender = (rowsFinishRender * cardViewHeight) + (rowsFinishRender * (calcCardViewMargin*2))
                if(loadNewPage){
                    if(calcViewCardHasRender < screenHeight) {
                        fetchMovie()
                    }else{
                        return handler.removeCallbacks(this)
                    }
                }
                return recheckMovieRenderHeight()
            }
        }, 2000)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        onDestroy()
    }
}