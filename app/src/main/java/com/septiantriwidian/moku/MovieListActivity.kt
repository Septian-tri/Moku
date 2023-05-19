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
    var cardViewLyParam : LayoutParams?
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
        this.cardViewLyParam = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_movie_list)

        val threadPolicy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        val fullScreenFlag = WindowManager.LayoutParams.FLAG_FULLSCREEN
        val intent = intent.extras
        val genreId = intent!!.getLong(IntentKey.GENRE_ID.name)
        val genreName = intent.getString(IntentKey.GENRE_NAME.name) as String
        val mediaMovie : String = intent.getString(IntentKey.MEDIA_MOVIE.name) as String
        val searchQuery : String = intent.getString(IntentKey.SEARCH_QUERY.name) as String
        val parentScroll : ScrollView = findViewById(R.id.parentScrollView)
        val getDisplayResolution = windowManager.defaultDisplay
        val displayResolutionPoint = Point()
            getDisplayResolution.getSize(displayResolutionPoint)
        val screenWidth = displayResolutionPoint.x
        val headerContainer : LinearLayout = findViewById(R.id.movieListHeader);
        val viewTitle = if(mediaMovie == MovieDetailMediaType.BY_ID_GENRE.name)  genreName!!.uppercase() else searchQuery

        apiService = ApiService(applicationContext, "id")
        window.setFlags(fullScreenFlag, fullScreenFlag)
        CustomActionBar(headerContainer, viewTitle, true, onBackPressedDispatcher).inflateHeader()
        StrictMode.setThreadPolicy(threadPolicy)

        //setup the grid view card for list movies
        scrollLayoutParent   = findViewById(R.id.parentLayoutMovieList)
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
        bufferAnimate = LayoutInflater.from(applicationContext).inflate(R.layout.buffer_progressbar_animate, null) as ProgressBar
        scrollLayoutParent.addView(bufferAnimate)

        //do endless scroll and load the movies lists
        val handler = Handler(Looper.getMainLooper())
        parentScroll.viewTreeObserver.addOnScrollChangedListener(object : ViewTreeObserver.OnScrollChangedListener{
            override fun onScrollChanged() {

                fun checkNextPage() : Boolean{
                    if(loadNewPage && totalMoviePage > 1 && movieStartPage <= totalMoviePage){
                        loadNewPage = false
                        movieStartPage++
                        fetchMovie(genreId, searchQuery, mediaMovie)
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
                    if(bufferAnimate.parent == null){
                        scrollLayoutParent.addView(bufferAnimate)
                        checkRepeatedLoadNextPage()
                    }

                }

            }

        })
        fetchMovie(genreId, searchQuery, mediaMovie)
    }

    private fun loadMovie(result : MoviesListResponseDTO) {
        this@MovieListActivity.runOnUiThread(Runnable {
            var totalImageLoad = 0
            var totalCardRender = viewCardPerLine
            val totalMovies     = result.results.size

            var scrollLayoutHorizontal = LinearLayout(applicationContext)
            scrollLayoutHorizontal.orientation = LinearLayout.HORIZONTAL

            //removing progressbar animation
            scrollLayoutParent.removeView(bufferAnimate)

            for (i in 0..(totalMovies-1)) {

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

    private fun fetchMovie(genreId : Long, query:String, mediaMovie : String){
        if(mediaMovie == MovieDetailMediaType.BY_ID_GENRE.name){
            apiService.fetchMoviesListByGenre(genreId, movieStartPage) { result ->
                loadMovie(result)
            }
        }else{
            apiService.fetchMovieSearch(URLEncoder.encode(query), movieStartPage) { result ->
                loadMovie(result)
            }
        }
    }
}