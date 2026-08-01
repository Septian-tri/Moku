package com.septiantriwidian.moku.service
import com.septiantriwidian.moku.utils.constant.ApiUrl
import okhttp3.*
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

class NetworkService {

    private val okHttpClient : OkHttpClient
    var headerHost : String
    var userAgent : String

    init {
        val trustAllCerts = arrayOf<TrustManager>(
            object : X509TrustManager {
                override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {
                }

                override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
                }

                override fun getAcceptedIssuers(): Array<X509Certificate> {
                    return arrayOf()
                }
            }
        )
        val sslContext: SSLContext = SSLContext.getInstance("SSL")
        sslContext.init(null, trustAllCerts, SecureRandom())
        val sslSocketFactory: SSLSocketFactory = sslContext.socketFactory;

        okHttpClient = OkHttpClient()
            .newBuilder()
            .sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
            .connectionSpecs(tlsConnection())
            .callTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(120, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .connectTimeout(120, TimeUnit.SECONDS)
            .build()
        this.headerHost = ApiUrl.MOVIE_HOST
        this.userAgent  = "Android (Linux; U; Android 33; fr; OPPO/CPH2197/mobile) com.backelite.vingtminutes/5.0.9.1"
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

    fun get(url : String, callback: Callback){

        val request = Request
            .Builder()
            .headers(headers())
            .get()
            .url(url)
            .build()

        val call = okHttpClient.newCall(request)
        call.enqueue(callback)
    }

    private fun headers() : Headers {
        return Headers
            .Builder()
            .add("Host", headerHost)
            .add("User-Agent", userAgent)
            .build()
    }

}