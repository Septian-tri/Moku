package com.septiantriwidian.moku

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.StrictMode
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.WindowManager.LayoutParams
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.septiantriwidian.moku.dto.SingleMovieGenreResponseDTO
import com.septiantriwidian.moku.adapter.SliderAdapterTrendingMovies
import com.septiantriwidian.moku.service.ApiService
import com.septiantriwidian.moku.view.CustomActionBar
import com.septiantriwidian.moku.utils.constant.IntentKey
import com.septiantriwidian.moku.utils.constant.MovieDetailMediaType
import com.septiantriwidian.moku.utils.constant.MoviesTrendingMedia
import com.septiantriwidian.moku.utils.constant.ViewCardMoviesSetting
import com.smarteist.autoimageslider.SliderView
import kotlinx.coroutines.Runnable

class MainActivity : AppCompatActivity() {

    lateinit var apiService : ApiService
    lateinit var sliderView : SliderView
    lateinit var genresListButtonContainer : LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val fullScreenFlag = LayoutParams.FLAG_FULLSCREEN
        val threadPolicy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        val singleMovieCardParams = FrameLayout.LayoutParams(ViewCardMoviesSetting.MOVIE_CARDVIEW_WIDTH, ViewCardMoviesSetting.MOVIE_CARDVIEW_HEIGHT)
        val handler = Handler(Looper.getMainLooper())
        val headerView : LinearLayout =  findViewById(R.id.mainActivityHeader)

        apiService = ApiService(applicationContext, "id")
        window.setFlags(fullScreenFlag, fullScreenFlag)

        CustomActionBar(headerView, null, false).inflateHeader()
        StrictMode.setThreadPolicy(threadPolicy)
        singleMovieCardParams.bottomMargin = ViewCardMoviesSetting.MOVIE_CARDVIEW_MARGIN

        //fetch whole trending movies for image slider
        sliderView = findViewById(R.id.moviesSliderCover)
        apiService.fetchTrendingMovies{result ->
            handler.post(Runnable {
                if(result.size > 0){
                    sliderView.setSliderAdapter(SliderAdapterTrendingMovies(result, this))
                    sliderView.slideToNextPosition()
                    sliderView.autoCycleDirection = SliderView.AUTO_CYCLE_DIRECTION_RIGHT
                    sliderView.startAutoCycle()
                }
            })
        }

        //fetch movies genres
        apiService.fetchGenresMovie {result ->
            handler.post(Runnable {
                genresListButtonContainer = findViewById(R.id.genresListButtonContainer)

                val buttonBgColor   = resources.getColor(R.color.blue_sky)
                val buttonTxtColor  = resources.getColor(R.color.white)
                val layoutParams    = LinearLayout.LayoutParams(
                    LayoutParams.WRAP_CONTENT,
                    LayoutParams.MATCH_PARENT
                )
                layoutParams.setMargins(2, 0, 2, 0)

                for(genre : SingleMovieGenreResponseDTO in result.genres){

                    val button    = Button(applicationContext)
                    val genreName = genre.name

                    button.setOnClickListener {
                        val movieListByGenres = Intent(applicationContext, MovieListActivity::class.java)
                        movieListByGenres.putExtra(IntentKey.GENRE_ID.name, genre.id)
                        movieListByGenres.putExtra(IntentKey.GENRE_NAME.name, genreName)
                        movieListByGenres.putExtra(IntentKey.MEDIA_MOVIE.name, MovieDetailMediaType.BY_ID_GENRE.name)
                        movieListByGenres.putExtra(IntentKey.SEARCH_QUERY.name, "")
                        startActivity(movieListByGenres)
                    }

                    button.text = genreName
                    button.layoutParams  = layoutParams
                    button.letterSpacing = 0.12f
                    button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12F)
                    button.setBackgroundColor(buttonBgColor)
                    button.setTextColor(buttonTxtColor)
                    button.setPadding(10, 0, 10, 0)
                    genresListButtonContainer.addView(button)

                }
            })

        }

        //fetch trending movies by media
        fetchTrendingMoviesByMedia(MoviesTrendingMedia.movie, R.id.trendingMoviesContainer)
        fetchTrendingMoviesByMedia(MoviesTrendingMedia.tv, R.id.trendingTvSeriesContainer)

    }

    private fun fetchTrendingMoviesByMedia(trendingMedia: MoviesTrendingMedia, trendingMoviesContainer : Int){
        apiService.trendingMedia = trendingMedia
        apiService.fetchTrendingMovies { results ->
            this@MainActivity.runOnUiThread(Runnable {
                val rootContainerTrendingMovie : LinearLayout = findViewById(trendingMoviesContainer)

                for(i in 0 .. (results.size-1)){

                    val singleMovie = results.get(i)
                    val layoutParams = LinearLayout.LayoutParams(ViewCardMoviesSetting.MOVIE_CARDVIEW_WIDTH, ViewCardMoviesSetting.MOVIE_CARDVIEW_HEIGHT)
                    val inflater = LayoutInflater.from(applicationContext).inflate(R.layout.movie_single_cardview, null)
                    val movieTitle : TextView = inflater.findViewById(R.id.singleMovieCardTitle)
                    val singleMovieCard : FrameLayout = inflater.findViewById(R.id.SingleMovieCardView)
                    val coverImage : ImageView = inflater.findViewById(R.id.singleMovieCardCover)
                    val rating : TextView = inflater.findViewById(R.id.singleMovieCardRatingText)
                    val releaseStatus : TextView = inflater.findViewById(R.id.singleMovieCardReleaseStatus)

                    layoutParams.setMargins(ViewCardMoviesSetting.MOVIE_CARDVIEW_MARGIN, 0, ViewCardMoviesSetting.MOVIE_CARDVIEW_MARGIN, 0)
                    inflater.layoutParams = layoutParams

                    apiService.fetchImage(singleMovie.poster_path){ imageResult ->
                        this@MainActivity.runOnUiThread(Runnable {
                            rating.text = String.format("%.1f/10", singleMovie.vote_average)
                            movieTitle.text = singleMovie.original_title?:singleMovie.title?:singleMovie.name
                            coverImage.setImageBitmap(imageResult)
                            singleMovieCard.removeView(inflater.findViewById(R.id.singleMovieCardProgressBar))

                            inflater.setOnClickListener {
                                val singleMovieDetail = Intent(applicationContext, SingleMovieDetailActivity::class.java)
                                singleMovieDetail.putExtra("singleMovie", singleMovie)
                                singleMovieDetail.flags = Intent.FLAG_ACTIVITY_NO_HISTORY
                                startActivity(singleMovieDetail)
                            }
                        })
                    }

                    rootContainerTrendingMovie.addView(inflater)
                }
            })

        }
    }

}