package com.septiantriwidian.moku

import android.content.Intent
import android.graphics.Point
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.StrictMode
import android.view.*
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.LinearLayout.LayoutParams
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import androidx.core.view.setMargins
import com.septiantriwidian.moku.dto.MovieCardUtilsDTO
import com.septiantriwidian.moku.dto.MoviesListResponseDTO
import com.septiantriwidian.moku.dto.SingleMovieResponseDTO
import com.septiantriwidian.moku.service.ApiService
import com.septiantriwidian.moku.utils.constant.IntentKey
import com.septiantriwidian.moku.utils.constant.MovieDetailMediaType
import com.septiantriwidian.moku.utils.helper.MovieCardUtils
import com.septiantriwidian.moku.view.ActionBar
import kotlinx.coroutines.Runnable
import java.net.URLEncoder
import java.util.*
import kotlin.math.floor

class MovieListActivity : AppCompatActivity() {
    private lateinit var movieCardUtilsDTO : MovieCardUtilsDTO
    private var isLoadingFetchMovies : Boolean = false
    private lateinit var scrollLayoutParent: ViewGroup
    private lateinit var bufferAnimate : ProgressBar
    private var lastRowMovies : LinearLayout? = null
    private var requestNextPage : Boolean = false
    private lateinit var apiService : ApiService
    private lateinit var scrollView : ScrollView
    private var movieStartPage: Long = 0
    private var totalMoviePage: Long = 0
    private var searchQuery : String = ""
    private var mediaMovie : String = ""
    private var genreId : Long = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_movie_list)
        movieCardUtilsDTO = MovieCardUtils(this).details()

        val intent = intent.extras!!
        var genreName : String? = null

        if(Objects.nonNull(intent.getString(IntentKey.MEDIA_MOVIE.name))){
            mediaMovie = intent.getString(IntentKey.MEDIA_MOVIE.name) as String
        }

        if(Objects.nonNull(intent.getString(IntentKey.SEARCH_QUERY.name))){
            searchQuery = intent.getString(IntentKey.SEARCH_QUERY.name) as String
        }

        if(Objects.nonNull(intent.getString(IntentKey.GENRE_NAME.name))){
            genreName = intent.getString(IntentKey.GENRE_NAME.name) as String
        }

        apiService = ApiService(applicationContext, "id")
        genreId = intent.getLong(IntentKey.GENRE_ID.name)

        val viewTitle = if(!Objects.isNull(genreName)  && MovieDetailMediaType.BY_ID_GENRE.name.equals(mediaMovie))  genreName?.uppercase() else searchQuery
        val threadPolicy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        val layoutScroll : LinearLayout = findViewById(R.id.parentLayoutMovieList)
        val parentScroll : ScrollView = findViewById(R.id.parentScrollView)
        val fullScreenFlag = WindowManager.LayoutParams.FLAG_FULLSCREEN
        val getDisplayResolution = windowManager.defaultDisplay
        val displayResolutionPoint = Point()

        getDisplayResolution.getSize(displayResolutionPoint)
        StrictMode.setThreadPolicy(threadPolicy)

        scrollView = parentScroll
        scrollLayoutParent = layoutScroll
        window.setFlags(fullScreenFlag, fullScreenFlag)

        //init buffer animate
        bufferAnimate = LayoutInflater.from(applicationContext).inflate(R.layout.buffer_progressbar_animate, null) as ProgressBar

        //do endless scroll and load the movies lists
        parentScroll.viewTreeObserver.addOnScrollChangedListener(object : ViewTreeObserver.OnScrollChangedListener{
            override fun onScrollChanged() {
                val calcMaxScroll = (layoutScroll.height-parentScroll.height)-movieCardUtilsDTO.getCardHeight()

                if(parentScroll.scrollY >= calcMaxScroll){
                    requestNextPage = true
                    fetchMovie()
                    println("$calcMaxScroll <>" + parentScroll.scrollY)
                }
            }
        })

        ActionBar(this, viewTitle, true) {
            finishAndRemoveTask()
        }
        fetchMovie()
    }

    private fun renderMovies(result : MoviesListResponseDTO) {
        totalMoviePage = result.total_pages
        val layParams = LayoutParams(movieCardUtilsDTO.getCardWidth(), movieCardUtilsDTO.getCardHeight())
        layParams.setMargins(movieCardUtilsDTO.getCardMargin())
        val totalWidthSingleCard = layParams.width
        this@MovieListActivity.runOnUiThread(Runnable {
            scrollLayoutParent.removeView(bufferAnimate)

            if(result.results.size <= 0 && Objects.isNull(lastRowMovies)){
                val errorState : FrameLayout = findViewById(R.id.notFoundState)

                scrollView.visibility = View.GONE
                errorState.visibility = View.VISIBLE
                return@Runnable
            }

            var row :LinearLayout = newLinearLayout()
            val totalMovies = result.results.size
            var iteration = totalWidthSingleCard
            var notYetRenderLeft = totalMovies
            var totalImageLoad = 0

            for((i, singleMovie:SingleMovieResponseDTO) in result.results.withIndex()){
                val viewCardInflater = LayoutInflater.from(applicationContext).inflate(R.layout.movie_single_cardview, null)
                val cardView : FrameLayout = viewCardInflater.findViewById(R.id.singleMovieCardParentView)
                val ratingMovie : TextView = viewCardInflater.findViewById(R.id.singleMovieCardRatingText)
                val coverMovie : ImageView = viewCardInflater.findViewById(R.id.singleMovieCardCover)
                val titleMovie : TextView = viewCardInflater.findViewById(R.id.singleMovieCardTitle)
                val adultState : TextView = viewCardInflater.findViewById(R.id.adultState)

                titleMovie.text = if(Objects.isNull(singleMovie.name)) singleMovie.title else singleMovie.name
                ratingMovie.text = String.format("%.1f/10", singleMovie.vote_average)
                adultState.text = if (singleMovie.adult) "+18"  else "";
                viewCardInflater.layoutParams = layParams

                println(singleMovie.toString())
                //insert card view to the last row movies if possible
                if(!Objects.isNull(lastRowMovies) && ((totalWidthSingleCard * lastRowMovies?.childCount!!) + totalWidthSingleCard) <= movieCardUtilsDTO.getScreenWidth()){
                    lastRowMovies?.addView(viewCardInflater)
                } else {
                    iteration+=totalWidthSingleCard
                    row.addView(viewCardInflater)

                    if(iteration >= movieCardUtilsDTO.getScreenWidth() || ((i+1) == totalMovies && notYetRenderLeft > 0)){
                        notYetRenderLeft -= row.childCount
                        iteration = totalWidthSingleCard
                        scrollLayoutParent.addView(row)

                        if((i+1) == totalMovies){
                            lastRowMovies = row
                        }
                        row = newLinearLayout()
                    }
                }

                apiService.fetchImage(if(Objects.isNull(singleMovie.poster_path)) "none" else singleMovie.poster_path) { result ->
                    this@MovieListActivity.runOnUiThread(Runnable {
                        val progressBar: ProgressBar = viewCardInflater.findViewById(R.id.singleMovieCardProgressBar)

                        cardView.removeView(progressBar)
                        coverMovie.setImageBitmap(result)
                        cardView.setOnClickListener {
                            val singleMovieDetail = Intent(applicationContext, SingleMovieDetailActivity().javaClass)
                            singleMovieDetail.putExtra("singleMovie", singleMovie)
                            startActivity(singleMovieDetail)
                        }

                        totalImageLoad++;

                        if(totalImageLoad >= totalMovies){ //waiting for all image cover loaded, and load next page wil be allowed
                            totalImageLoad = 0
                            isLoadingFetchMovies = false

                            //if the results of movies can't meet the maximum screen height
                            if(requestNextPage || scrollLayoutParent.height < movieCardUtilsDTO.getScreenHeight()){
                                requestNextPage = false;
                                fetchMovie()
                            }
                        }
                    })
                }
            }
        })
    }

    private fun newLinearLayout():LinearLayout{
        val linLay = LinearLayout(applicationContext)
        val layParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)

        linLay.orientation = LinearLayout.HORIZONTAL
        layParams.gravity = Gravity.CENTER
        linLay.layoutParams = layParams
        return linLay
    }

    private fun fetchMovie(){
        bufferAnimate()

        if(!isLoadingFetchMovies && movieStartPage <= totalMoviePage){
            isLoadingFetchMovies = true
            movieStartPage++

            if(MovieDetailMediaType.BY_ID_GENRE.name.equals(mediaMovie)){
                apiService.fetchMoviesListByGenre(genreId, movieStartPage) { result ->
                    renderMovies(result)
                }
            }else{
                apiService.fetchMovieSearch(URLEncoder.encode(searchQuery), movieStartPage) { result ->
                    renderMovies(result)
                }
            }
        }
    }

    private fun bufferAnimate(){
        if(bufferAnimate.parent == null) {
            scrollLayoutParent.addView(bufferAnimate)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finishAndRemoveTask()
    }
}