package com.septiantriwidian.moku

import android.content.Intent
import android.graphics.Color
import android.net.http.SslError
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.StrictMode
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.webkit.*
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RatingBar
import android.widget.ScrollView
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import com.septiantriwidian.moku.dto.SingleMovieGenreResponseDTO
import com.septiantriwidian.moku.dto.SingleMovieResponseDTO
import com.septiantriwidian.moku.dto.SingleMovieReviewDetailResponsesDTO
import com.septiantriwidian.moku.service.ApiService
import com.septiantriwidian.moku.service.NetworkService
import com.septiantriwidian.moku.view.CustomActionBar
import com.septiantriwidian.moku.utils.constant.ApiUrl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.cancel

class SingleMovieDetailActivity : AppCompatActivity() {

    lateinit var webViewTrailer: WebView
    lateinit var apiService : ApiService
    lateinit var rootReviewLayout : LinearLayout
    lateinit var bufferAnimate : View

    var movieReviewsStartPage : Long
    var totalReviewsPages : Long
    var totalReviewsResults : Int
    var movieReviewFinishRender : Long
    var nextReviewsPages : Boolean

    init {
        this.movieReviewsStartPage = 1
        this.totalReviewsPages =  1
        this.totalReviewsResults = 0
        this.movieReviewFinishRender = 0
        this.nextReviewsPages = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_single_movie_detail)

        val singleMovie = intent.extras!!.getSerializable("singleMovie") as SingleMovieResponseDTO
        val threadPolicy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        val fullScreenFlag = WindowManager.LayoutParams.FLAG_FULLSCREEN
        val movieTitle = singleMovie.name ?: singleMovie.title
        val rootScrollMovieDetail : ScrollView = findViewById(R.id.rootScrollViewMovieDetail)
        val movieTitleDetail : TextView = findViewById(R.id.titleMovieDetail)
        val movieRating : RatingBar = findViewById(R.id.ratingBarMovieDDetail)
        val movieRatingTxt : TextView = findViewById(R.id.ratingTextMovieDetail)
        val viewerTarget : TextView = findViewById(R.id.viewerTarget)
        val movieOverview : TextView = findViewById(R.id.movieOverview)
        val movieMedia : TextView = findViewById(R.id.movieMedia)
        val header : LinearLayout = findViewById(R.id.movieDetailHeader)

        apiService = ApiService(applicationContext, "id")
        window.setFlags(fullScreenFlag, fullScreenFlag)
        CustomActionBar(header, movieTitle, true).inflateHeader()
        StrictMode.setThreadPolicy(threadPolicy)

        movieRatingTxt.text   = String.format("%.1f/10", singleMovie.vote_average)
        movieRating.rating    = (singleMovie.vote_average/2).toFloat()
        movieTitleDetail.text = movieTitle
        viewerTarget.text     = if(singleMovie.adult) "+18" else "SU|BO"
        movieOverview.text    = singleMovie.overview
        movieMedia.text       = if (singleMovie.media_type == "tv") "Serial Televisi" else "Layar Lebar"

        //fetch more detail movie
        apiService.fetchMovieDetail(singleMovie.id){ result ->
            this@SingleMovieDetailActivity.runOnUiThread(Runnable {
                val movieDurationTV : TextView = findViewById(R.id.movieDuration)
                val movieTagLineTV : TextView = findViewById(R.id.movieTagLine)
                val movieDetailTable : TableLayout = findViewById(R.id.tableMovieDetail)
                val movieTagGenresContainer : LinearLayout = findViewById(R.id.genresTagMovieDetail)
                val movieTagGenres = result.genres
                val durationHours  = Math.floor((result.runtime/60).toDouble()).toInt()
                val durationMinute = result.runtime-(durationHours*60)
                val durationSecond = Math.floor((durationMinute/60).toDouble()).toInt()*60
                val durationMinutePrefix = if(durationMinute <= 9) "0$durationMinute" else durationMinute
                val durationHourPrefix   = if(durationHours <= 9) "0$durationHours" else durationHours
                val durationSecondPrefix = if(durationSecond <= 9) "0$durationSecond" else durationSecond
                val finalDuration = "$durationHourPrefix:$durationMinutePrefix:$durationSecondPrefix"
                val movieTagLine = if (result.tagline.equals("")) "Tidak Ada Tagline" else result.tagline

                movieDurationTV.text = finalDuration
                movieTagLineTV.text = movieTagLine

                for(tagGenre : SingleMovieGenreResponseDTO in movieTagGenres){

                    val buttonGenresInflater = LayoutInflater.from(applicationContext).inflate(R.layout.movie_genres_button_template, null)
                    val genreTagButton : TextView = buttonGenresInflater.findViewById(R.id.buttonGenres)

                    genreTagButton.text = tagGenre.name.uppercase()
                    movieTagGenresContainer.addView(buttonGenresInflater)

                    genreTagButton.setOnClickListener {
                        val intent = Intent(applicationContext, MovieListActivity::class.java)
                            .putExtra("genreName", tagGenre.name)
                            .putExtra("genreId", tagGenre.id)
                        startActivity(intent)
                    }

                }

                movieDetailTable.addView(rowTable("Judul ", movieTitle))
                movieDetailTable.addView(rowTable("Judul Asli", singleMovie.original_title))
                movieDetailTable.addView(rowTable("Status Rilis", result.status))
                movieDetailTable.addView(rowTable("Tanggal Rilis", singleMovie.release_date))
                movieDetailTable.addView(rowTable("Anggaran", result.budget.toString()))
                movieDetailTable.addView(rowTable("Pendapatan", result.revenue.toString()))
            })

        }

        //fetch image for mini cover
        val miniCover : ImageView = findViewById(R.id.movieMiniCover)
        apiService.fetchImage(singleMovie.poster_path){ result ->
            this@SingleMovieDetailActivity.runOnUiThread(Runnable {
                val parentMiniCover = miniCover.parent as ViewGroup
                parentMiniCover.removeView(parentMiniCover.findViewById(R.id.movieMiniCoverLoading))
                miniCover.setImageBitmap(result)
            })
        }

        //fetch image for backdrop background
        val scrollView : ImageView = findViewById(R.id.movieDetailBackgroundImage)
        if(singleMovie.backdrop_path !== null){
            apiService.fetchImage(singleMovie.backdrop_path){ imageResult ->
                this@SingleMovieDetailActivity.runOnUiThread(Runnable { scrollView.setImageBitmap(imageResult) })
            }
        }

        //fetch movie trailer
        apiService.fetchMovieTrailer(singleMovie.id) { result ->

            var trailerKeyId = ""
            var siteSourceTrailer = ""

            for (i in 0 .. (result.results.size-1)){

                val singleResultTrailer = result.results.get(i)

                if(singleResultTrailer.type.equals("Trailer")){
                    trailerKeyId = singleResultTrailer.key
                    siteSourceTrailer = singleResultTrailer.site
                }

                //change key trailer if official trailer available
                if(singleResultTrailer.type.equals("Trailer") && singleResultTrailer.official){
                    trailerKeyId = singleResultTrailer.key
                    siteSourceTrailer = singleResultTrailer.site
                    break
                }

            }

            if(siteSourceTrailer.equals("YouTube")) {
                this@SingleMovieDetailActivity.runOnUiThread(Runnable {
                    val trailerUri = String.format(ApiUrl.YOUTUBE_MOVIE_TRAILER_URI, trailerKeyId)
                    var hardwareAccelerateFlag = WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED

                    window.setFlags(hardwareAccelerateFlag, hardwareAccelerateFlag)

                    //activated the web view for watching the movie trailer
                    webViewTrailer = findViewById(R.id.movieTrailer)
                    webViewTrailer.settings.javaScriptEnabled = true
                    webViewTrailer.settings.setRenderPriority(WebSettings.RenderPriority.HIGH)
                    webViewTrailer.settings.userAgentString = NetworkService().userAgent
                    webViewTrailer.settings.useWideViewPort = true
                    webViewTrailer.settings.cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
                    webViewTrailer.webChromeClient = WebChromeClient()
                    webViewTrailer.webViewClient = object : WebViewClient(){

                        override fun onPageFinished(view: WebView?, url: String?) {
                            webViewTrailer.removeView(webViewTrailer.findViewById(R.id.layoutWebViewLoading))
                        }

                        override fun onReceivedSslError(
                            view: WebView?,
                            handler: SslErrorHandler?,
                            error: SslError?
                        ) {
                            handler!!.proceed()
                        }

                    }
                    webViewTrailer.loadUrl(trailerUri)
                })
            }

        }

        //load movie reviews and do endless scroll
        bufferAnimate = LayoutInflater.from(applicationContext).inflate(R.layout.buffer_progressbar_animate, null)
        rootReviewLayout = findViewById(R.id.rootReviewLayout)
        rootReviewLayout.addView(bufferAnimate)
        rootScrollMovieDetail.viewTreeObserver.addOnScrollChangedListener {
            if( rootScrollMovieDetail.scrollY >= (rootScrollMovieDetail.layoutParams.height-20) &&
                totalReviewsPages > 1 &&
                movieReviewsStartPage <= totalReviewsPages &&
                nextReviewsPages){

                nextReviewsPages = false
                movieReviewsStartPage++
                rootReviewLayout.addView(bufferAnimate)
                loadMovieReviews(singleMovie)
            }
        }
        loadMovieReviews(singleMovie)

    }

    fun rowTable(titleText : String, valueText : String?) : TableRow {

        val text1 = TextView(applicationContext)
        val text2 = TextView(applicationContext)
        val row  = TableRow(applicationContext)
        val textColor = resources.getColor(R.color.white)

        text1.setPadding(0, 5, 10, 5)
        text1.textSize = 12f
        text1.text = titleText
        text1.setTextColor(textColor)

        text2.textSize = 12f
        text2.text = valueText ?: "-"
        text2.setTextColor(textColor)

        row.addView(text1)
        row.addView(text2)

        return row
    }

    fun movieReviewLayoutTemplate(reviews : SingleMovieReviewDetailResponsesDTO) : View{

        val inflater : View = LayoutInflater.from(applicationContext).inflate(R.layout.movie_reveiw_template, null)
        val reviewCreated : TextView = inflater.findViewById(R.id.dateReviewCreated)
        val authorReviewName : TextView = inflater.findViewById(R.id.authorReview)
        val reviewContents : TextView = inflater.findViewById(R.id.reviewContent)
        val authorAvatar : ImageView = inflater.findViewById(R.id.avatarReviewImage)

        authorReviewName.text = reviews.author
        reviewCreated.text = reviews.created_at.toString()
        reviewContents.text = Html.fromHtml(reviews.content)
        authorAvatar.setImageBitmap(reviews.author_details.avatar_image)

        return inflater
    }

    private fun loadMovieReviews(singleMovie : SingleMovieResponseDTO){

        apiService.fetchMovieReviews(singleMovie.id, movieReviewsStartPage){ reviews ->

            val reviewsTotal = reviews.results.size

            this@SingleMovieDetailActivity.runOnUiThread(object : Runnable{
                override fun run() {
                    rootReviewLayout.removeView(bufferAnimate)
                }
            })

            totalReviewsPages = reviews.total_pages
            totalReviewsResults = reviewsTotal

            this@SingleMovieDetailActivity.runOnUiThread(object : Runnable{
                override fun run() {

                    for(i in 0 .. (reviewsTotal-1)){

                        val singleReviews = reviews.results.get(i)

                        apiService.fetchAvatarImage(singleReviews.author_details.avatar_path){ avatarResult ->

                            this@SingleMovieDetailActivity.runOnUiThread(object : Runnable{
                                override fun run() {

                                    movieReviewFinishRender++

                                    singleReviews.author_details.avatar_image = avatarResult
                                    rootReviewLayout.addView(movieReviewLayoutTemplate(singleReviews))

                                    if(movieReviewFinishRender >= reviewsTotal){
                                        movieReviewFinishRender = 0;
                                        nextReviewsPages = true
                                    }

                                }

                            })

                        }

                    }

                }

            })

        }
    }

    override fun onBackPressed() {

        if(::webViewTrailer.isInitialized){
            webViewTrailer.clearHistory()
            webViewTrailer.destroyDrawingCache()
            webViewTrailer.clearCache(true)
            webViewTrailer.destroy()
        }

        CoroutineScope(Dispatchers.IO).cancel()
        Handler(Looper.getMainLooper()).removeCallbacksAndMessages(null)
        finish()
        onDestroy()
    }

}