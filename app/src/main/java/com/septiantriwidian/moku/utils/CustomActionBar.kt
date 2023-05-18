package com.septiantriwidian.moku.utils

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.OnBackPressedDispatcher
import androidx.appcompat.app.AppCompatActivity
import com.septiantriwidian.moku.MovieListActivity
import com.septiantriwidian.moku.R
import com.septiantriwidian.moku.utils.constant.IntentKey
import com.septiantriwidian.moku.utils.constant.MovieDetailMediaType

class CustomActionBar (parentView : View, title : String?, backButton : Boolean, onBackPressedDispatcher: OnBackPressedDispatcher) : AppCompatActivity(){

    private var parentView : ViewGroup
    private var title : String?
    private var backButton : Boolean
    private var onBackPressedDispatcher : OnBackPressedDispatcher

    init {
        this.parentView = parentView as ViewGroup
        this.title = title
        this.backButton = backButton
        this.onBackPressedDispatcher = onBackPressedDispatcher
    }

    fun inflateHeader(){
        val inflateHeader = LayoutInflater.from(parentView.context).inflate(R.layout.custom_action_bar, parentView)
        val actionBarTitle : TextView = inflateHeader.findViewById(R.id.actionBarTitle)
        val backButtonView : Button = inflateHeader.findViewById(R.id.backOptions)
        val searchField : EditText = inflateHeader.findViewById(R.id.searchField)
        val searchButton : Button = inflateHeader.findViewById(R.id.actionBarSearchButton)

        if(title != null){
            actionBarTitle.text = title;
        }else{
            actionBarTitle.visibility = TextView.GONE
        }

        if(!backButton){
            backButtonView.visibility= Button.GONE
        }else{
            backButtonView.setOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }
        }

        //set action button search on keyboard and on screen
        searchButton.setOnClickListener{
            searchMovie(searchField.text.toString())
            searchField.onEditorAction(EditorInfo.IME_ACTION_DONE)
        }

        searchField.setOnEditorActionListener { _, actionId, event ->
            if(actionId == EditorInfo.IME_ACTION_SEARCH){
                searchMovie(searchField.text.toString())
            }
            false
        }
    }

    private fun searchMovie(query : String){
        val intent = Intent(parentView.context, MovieListActivity::class.java)
            intent.putExtra(IntentKey.GENRE_ID.name, "")
            intent.putExtra(IntentKey.GENRE_NAME.name, "")
            intent.putExtra(IntentKey.MEDIA_MOVIE.name, MovieDetailMediaType.BY_SEARCH_QUERY.name)
            intent.putExtra(IntentKey.SEARCH_QUERY.name, query)
            parentView.context.startActivity(intent)
    }

}