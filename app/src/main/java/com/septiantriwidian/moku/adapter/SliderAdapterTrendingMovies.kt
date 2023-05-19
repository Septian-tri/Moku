package com.septiantriwidian.moku.adapter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RatingBar
import android.widget.TextView
import com.septiantriwidian.moku.R
import com.septiantriwidian.moku.SingleMovieDetailActivity
import com.septiantriwidian.moku.dto.SingleMovieResponseDTO
import com.septiantriwidian.moku.service.ApiService
import com.smarteist.autoimageslider.SliderViewAdapter

class SliderAdapterTrendingMovies ( moviesList : ArrayList<SingleMovieResponseDTO>) : SliderViewAdapter<SliderAdapterTrendingMovies.SliderViewHolder>() {

    lateinit var apiService : ApiService
    var moviesList : ArrayList<SingleMovieResponseDTO>

    init {
        this.moviesList =  moviesList
    }

    override fun getCount(): Int {
        return moviesList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup?): SliderViewHolder {
        return SliderViewHolder(
                LayoutInflater
                    .from(parent!!.context)
                    .inflate(R.layout.movie_single_cover_slider, null)
        )
    }

    override fun onBindViewHolder(viewHolder: SliderViewHolder, position : Int){

        val rootView = viewHolder.itemView.rootView
        val context = rootView.context
        val intent = Intent(context, SingleMovieDetailActivity::class.java)
        val movie : SingleMovieResponseDTO = moviesList[position]

        apiService = ApiService(context, "id")
        apiService.fetchImage(movie.poster_path){ resultImage ->
            (context as Activity).runOnUiThread {

                val animateBufferParent = viewHolder.bufferAnimate.parent

                if(animateBufferParent != null){
                    (animateBufferParent as ViewGroup).removeView(viewHolder.bufferAnimate)
                }

                viewHolder.movieImgeCover.setImageBitmap(resultImage)
                viewHolder.movieTitle.text = movie.name ?: movie.title
                viewHolder.movieAdditionalTxt.text = movie.media_type
                viewHolder.movieRatingTxt.text = String.format("%.1f", movie.vote_average)
                viewHolder.ratingBar.rating = movie.vote_average.toFloat()/2

                viewHolder.movieImgeCover.setOnClickListener{
                    movie.imageCover = null //reassign null image cover before sending to another activity
                    intent.putExtra("singleMovie", movie)
                    intent.flags = Intent.FLAG_ACTIVITY_NO_HISTORY
                    context.startActivity(intent)
                    movie.imageCover = resultImage //recovering image cover after sending movie object to another activity
                }

            }
        }

    }

    class SliderViewHolder(itemView: View) : ViewHolder(itemView){

        var movieImgeCover   : ImageView
        var movieTitle       : TextView
        var movieRatingTxt     : TextView
        var movieAdditionalTxt : TextView
        var ratingBar        : RatingBar
        var bufferAnimate : ProgressBar

        init {
            movieImgeCover   = itemView.findViewById(R.id.movieImageCoverSlider)
            movieTitle       = itemView.findViewById(R.id.movieTitleImageCoverSlider)
            movieRatingTxt     = itemView.findViewById(R.id.movieRatingTextImageSlider)
            movieAdditionalTxt = itemView.findViewById(R.id.addtionalMovieTxtImageCoverSlider)
            ratingBar          = itemView.findViewById(R.id.ratingBar)
            bufferAnimate = itemView.findViewById(R.id.bufferImageCoverSlider)
        }

    }

}