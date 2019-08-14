package br.com.assestment.android.docsearch.services

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ServicesHelper {

    companion object {
        val AUTH_BASE_URL = "https://auth.staging.vivy.com/"
        val API_BASE_URL = "https://api.staging.vivy.com/api/"

        fun client(isAuth: Boolean): Retrofit? {
            val baseUrl = if (isAuth) AUTH_BASE_URL else API_BASE_URL
            return Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(baseUrl)
                .build()
        }
    }
}