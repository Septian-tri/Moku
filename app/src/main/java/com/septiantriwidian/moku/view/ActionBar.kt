package com.septiantriwidian.moku.view

import android.app.Activity
import android.content.Intent
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.TextView.OnEditorActionListener
import com.septiantriwidian.moku.MovieListActivity
import com.septiantriwidian.moku.R
import com.septiantriwidian.moku.utils.constant.IntentKey
import java.util.*


class ActionBar (activity: Activity, title:String?, showBackButton:Boolean, clearCurrentTask: Boolean, callback: ActionBarCallback?) {
    private var callback: ActionBarCallback?
    private var clearCurrentTask :Boolean
    private var showBackButton:Boolean
    private var activity : Activity
    private var title: String?

    init {
        this.clearCurrentTask = clearCurrentTask
        this.showBackButton = showBackButton
        this.callback = callback
        this.activity = activity
        this.title = title
        doAction()
    }

    private fun doAction()  {
        if(!Objects.isNull(activity)){
            if(!Objects.isNull(activity.findViewById(R.id.actionBarSearchButton)) && !Objects.isNull(activity.findViewById(R.id.searchField))){
                val searchBtn : Button = activity.findViewById(R.id.actionBarSearchButton)
                val searchQuery : EditText = activity.findViewById(R.id.searchField)

                searchBtn.setOnClickListener(View.OnClickListener {
                    doSearchMovie(searchQuery.text.toString())
                })

                searchQuery.setOnEditorActionListener(OnEditorActionListener { v, actionId, _ ->
                    if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                        doSearchMovie(v.text.toString())
                        return@OnEditorActionListener true
                    }
                    false
                })
            }

            if(showBackButton && !Objects.isNull(callback)){
                val backButton : Button = activity.findViewById(R.id.backButton)

                backButton.visibility = View.VISIBLE
                backButton.setOnClickListener(View.OnClickListener { view ->
                    try{
                        callback?.onBackPressed(view)
                    }catch (e : Exception){
                        Log.println(Log.ERROR, "ActionBar", e.message.toString())
                        e.printStackTrace()
                    } finally {
                        activity.onBackPressed()
                    }
                })
            }

            if(!Objects.isNull(title) && !"".equals(title)){
                val titleView : TextView = activity.findViewById(R.id.actionBarTitle)

                titleView.visibility = View.VISIBLE
                titleView.text = title
            }
        }
    }

    private fun doSearchMovie(query : String) {
        if(!"".equals(query)){
            val movieListByGenres = Intent(activity, MovieListActivity::class.java)
            movieListByGenres.putExtra(IntentKey.SEARCH_QUERY.name, query)

            if(clearCurrentTask){
                activity.finishAndRemoveTask()
            }
            activity.startActivity(movieListByGenres)
        }
    }
}