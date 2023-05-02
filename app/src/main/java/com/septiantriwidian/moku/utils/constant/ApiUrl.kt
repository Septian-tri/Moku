package com.septiantriwidian.moku.utils.constant

class ApiUrl(){

    /**
     * please use string format for calling constant variable at below
     * @param MOVIE_TRAILER_ENP apply string format value with number data type
     * @param MOVIE_DETAIL_ENP apply string format value with number data type
     * @param YOUTUBE_MOVIE_TRAILER_URI apply string format value with string data type
     * @param MOVIE_REVIEW_ENP apply string format value with number data type
     * @param MOVIE_TRENDING_ENP apply string format value with string data type base on MoviesTrendingMedia
     * */

    companion object {
        private const val API_SECRET_KEY          = "fca7ef4c108a912f2d599e9b83e64f8f"
        private const val MOVIE_HOST_SCHEME       = "https://"
        private const val MOVIE_MAIN_ENP          = "/3"
                const val MOVIE_HOST              = "api.themoviedb.org"
        private const val MOVIE_HOST_URL          = "$MOVIE_HOST_SCHEME$MOVIE_HOST$MOVIE_MAIN_ENP"
                const val AVATAR_IMAGE_HOST       = "secure.gravatar.com"
                const val MOVIE_PAGE_PARAM        = "&page="
                const val MOVIE_LANG_PARAM        = "&language="
                const val IMAGE_HOST              = "image.tmdb.org"
                const val API_KEY_PARAM           = "?api_key=$API_SECRET_KEY"
        private const val HOST_IMAGE_URL          = "$MOVIE_HOST_SCHEME$IMAGE_HOST"
                const val AVATAR_IMAGE_ENP        = "$HOST_IMAGE_URL/t/p/w200"
                const val IMAGE_COVER_ENP         = "$HOST_IMAGE_URL/t/p/w500"
                const val MOVIE_LIST_GENRES_ENP   = "$MOVIE_HOST_URL/genre/movie/list$API_KEY_PARAM"
                const val MOVIE_TRENDING_ENP      = "$MOVIE_HOST_URL/trending/%s/day$API_KEY_PARAM"
                const val MOVIE_LIST_BY_GENRE_ENP = "$MOVIE_HOST_URL/discover/movie/$API_KEY_PARAM&with_genres="
                const val MOVIE_TRAILER_ENP       = "$MOVIE_HOST_URL/movie/%d/videos$API_KEY_PARAM"
                const val MOVIE_DETAIL_ENP        = "$MOVIE_HOST_URL/movie/%d$API_KEY_PARAM"
                const val MOVIE_REVIEWS_ENP       = "$MOVIE_HOST_URL/movie/%d/reviews$API_KEY_PARAM$MOVIE_PAGE_PARAM%d"
                const val YOUTUBE_MOVIE_TRAILER_URI = "https://www.youtube.com/embed/%s?rel=0"
    }

}