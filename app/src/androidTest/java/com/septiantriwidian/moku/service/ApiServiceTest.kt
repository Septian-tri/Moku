package com.septiantriwidian.moku.service

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.septiantriwidian.moku.utils.constant.ApiUrl
import io.mockk.every
import kotlinx.coroutines.runBlocking
import okhttp3.Callback
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import okio.Buffer
import org.junit.After
import org.junit.Before
import org.junit.Test

import org.junit.Assert.*
import org.junit.runner.RunWith
import java.nio.charset.Charset

@RunWith(AndroidJUnit4::class)
class ApiServiceTest {

    lateinit var mockWebServer: MockWebServer
    lateinit var apiService : ApiService

    @Before
    fun setUp() {
        mockWebServer = MockWebServer()
        apiService = ApiService(InstrumentationRegistry.getInstrumentation().targetContext, "id")
        mockWebServer.start(8888)
    }

    @After
    fun tearDown() {
        mockWebServer.close()
    }


    /**
     * if fetch image successful load the url image
     * fetchImage should be return the image originate from url
     */
    @Test
    fun fetchImage() = runBlocking {

        val uri = "${ApiUrl.IMAGE_COVER_ENP}/pB8BM7pdSp6B6Ih7QZ4DrQ3PmJK.jpg"
        val imageBuffer = InstrumentationRegistry.getInstrumentation().targetContext.assets.open("pB8BM7pdSp6B6Ih7QZ4DrQ3PmJK.jpg")
        val expectedImage = BitmapFactory.decodeStream(imageBuffer)

//        mockWebServer.setDispatcher(object : Dispatcher() {
//            override fun dispatch(request: RecordedRequest?): MockResponse {
//                return MockResponse()
//                       .setResponseCode(200)
//                       .setHeader("content-type" ,"text/html")
//                       .setBody(imageBuffer.readBytes())
//            }
//        })
//        mockWebServer.url(uri)
//        apiService.baseUri = uri

//        every {
//
//            apiService.fetchImage(uri)
//
//        }
    }

}