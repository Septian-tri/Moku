package com.septiantriwidian.moku.service
import com.septiantriwidian.moku.utils.constant.ApiUrl
import okhttp3.*
import java.util.Arrays
import java.util.concurrent.TimeUnit

class NetworkService {

    private val okHttpClient : OkHttpClient
    var headerHost : String
    var userAgent : String

    init {
        okHttpClient = OkHttpClient()
            .newBuilder()
            .connectionSpecs(tlsConnection())
            .callTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(120, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .connectTimeout(120, TimeUnit.SECONDS)
            .build()
        this.headerHost = ApiUrl.MOVIE_HOST
        this.userAgent  = "Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:109.0) Gecko/20100101 Firefox/110.0"
    }

    private fun tlsConnection() : ArrayList<ConnectionSpec> {

        val connectionSpecList = ArrayList<ConnectionSpec>()
        val connectionSpecTls  = ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
        .allEnabledTlsVersions()
        .allEnabledCipherSuites()
        .build()

        val connectionSpecClearText = ConnectionSpec.Builder(ConnectionSpec.CLEARTEXT).build()

        connectionSpecList.add(connectionSpecTls)
        connectionSpecList.add(connectionSpecClearText)

        return connectionSpecList
    }

    fun GET(url : String, callback: Callback) : Call{

        val request = Request
            .Builder()
            .headers(headers())
            .get()
            .url(url)
            .build()

        val call = okHttpClient.newCall(request)
            call.enqueue(callback)

        return call
    }

    fun headers() : Headers {
        return Headers
            .Builder()
            .add("Host", headerHost)
            .add("User-Agent", userAgent)
            .build()
    }

}