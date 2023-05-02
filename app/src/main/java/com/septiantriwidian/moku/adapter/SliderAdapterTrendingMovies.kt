package com.septiantriwidian.moku.adapter

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import com.septiantriwidian.moku.R
import com.septiantriwidian.moku.SingleMovieDetailActivity
import com.septiantriwidian.moku.dto.SingleMovieResponseDTO
import com.septiantriwidian.moku.service.ApiService
import com.smarteist.autoimageslider.SliderViewAdapter

class SliderAdapterTrendingMovies (context: Context, moviesList : ArrayList<SingleMovieResponseDTO>) : SliderViewAdapter<SliderAdapterTrendingMovies.SliderViewHolder>() {

    lateinit var apiService : ApiService
    var moviesList : ArrayList<SingleMovieResponseDTO>
    var context : Context

    init {
        this.moviesList =  moviesList
        this.context = context
    }

    override fun getCount(): Int {
        return moviesList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup?): SliderViewHolder {
        val inflate : View = LayoutInflater
            .from(parent!!.context)
            .inflate(R.layout.movie_single_cover_slider, null)

        return SliderViewHolder(inflate)
    }

    override fun onBindViewHolder(viewHolder: SliderViewHolder, position : Int){

        apiService = ApiService(context, "id")

        val handler = Handler(Looper.getMainLooper())
        val movie : SingleMovieResponseDTO = moviesList.get(position)
        val movieName : String = movie.name ?: movie.title

        viewHolder.movieTitle.text = movieName
        viewHolder.movieAdditionalTxt.text = movie.media_type
        viewHolder.movieRatingTxt.text = String.format("%.1f", movie.vote_average)
        viewHolder.ratingBar.rating = movie.vote_average.toFloat()/2

        apiService.fetchImage(movie.poster_path){ resultImage ->
            handler.post(object : Runnable {
                override fun run() {
                    viewHolder.movieImgeCover.setImageBitmap(resultImage)
                    viewHolder.itemView.setOnClickListener(object : OnClickListener{
                        override fun onClick(v: View?) {
                            try{

                                val tempMovieCOver = movie.imageCover //hold the temporary image cover for restoring cover image after changing to null before send to another activity

                                movie.imageCover = null

                                val intent = Intent(context, SingleMovieDetailActivity().javaClass)
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                                intent.putExtra("singleMovie", movie)
                                context.startActivity(intent)

                                movie.imageCover = tempMovieCOver //recovering image cover after sending movie object to another activity

                            }catch (e : Exception){
                                println(e.localizedMessage)
                                e.printStackTrace()
                            }

                        }
                    })
                }
            })
        }

    }

    class SliderViewHolder(itemView: View) : ViewHolder(itemView){

        var movieImgeCover   : ImageView
        var movieTitle       : TextView
        var movieRatingTxt     : TextView
        var movieAdditionalTxt : TextView
        var ratingBar        : RatingBar

        init {
            movieImgeCover   = itemView.findViewById(R.id.movieImageCoverSlider)
            movieTitle       = itemView.findViewById(R.id.movieTitleImageCoverSlider)
            movieRatingTxt     = itemView.findViewById(R.id.movieRatingTextImageSlider)
            movieAdditionalTxt = itemView.findViewById(R.id.addtionalMovieTxtImageCoverSlider)
            ratingBar          = itemView.findViewById(R.id.ratingBar)
        }

    }

}