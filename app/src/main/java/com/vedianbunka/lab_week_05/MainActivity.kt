package com.vedianbunka.lab_week_05

import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.vedianbunka.lab_week_05.api.CatApiService
import com.vedianbunka.lab_week_05.model.ImageData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        getCatImageResponse()
    }

    private val retrofit by lazy{
        Retrofit.Builder()
            .baseUrl("https://api.thecatapi.com/v1/")
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
    }

    private val catApiService by lazy {
        retrofit.create(CatApiService::class.java)
    }

    private val apiResponseView: TextView by lazy {
        findViewById(
            R.id.api_response
        )
    }

    private val imageResultView : ImageView by lazy {
        findViewById<ImageView>(R.id.image_result)
    }

    private val imageLoader: ImageLoader by lazy {
        GlideLoader(this)
    }

    private fun getCatImageResponse() {
        val call = catApiService.searchImages(
            1,
            "full"
        )
        call.enqueue(object : Callback<List<ImageData>> {
            override fun onFailure(call: Call<List<ImageData>>, t: Throwable) {
                Log.e(
                    MAIN_ACTIVITY,
                    "Failed to get response", t
                )
            }
            override fun onResponse(
                call: Call<List<ImageData>>, response:
                Response<List<ImageData>>
            ) {
                if (response.isSuccessful) {
                    val image = response.body()
                    val firstImage = image?.firstOrNull()?.imageUrl.orEmpty()
                    val breed = image?.firstOrNull()?.breeds?.firstOrNull()?.name ?: "unknown"
                    if(firstImage.isNotBlank()){
                        imageLoader.loadImage(firstImage, imageResultView)
                    } else {
                        Log.d(MAIN_ACTIVITY, "Missing Image URL")
                    }
                    Log.d("DEBUG", "Image data: $image")
                    apiResponseView.text = getString(R.string.image_placeholder,
                        firstImage) + "\n" + getString(R.string.breed_placeholder, breed)
                } else {
                    Log.e(
                        MAIN_ACTIVITY,
                        "Failed to get response\n" +
                                response.errorBody()?.string().orEmpty()
                    )
                }
            }
        })
    }

    companion object {
        const val MAIN_ACTIVITY = "MAIN_ACTIVITY"
        const val DEBUG = "DEBUG"
    }
}