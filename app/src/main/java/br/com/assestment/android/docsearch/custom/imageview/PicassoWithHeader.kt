package br.com.socialbank.android.socialpartner.custom.picasso

import android.content.Context
import com.jakewharton.picasso.OkHttp3Downloader
import com.squareup.picasso.Picasso
import okhttp3.OkHttpClient

internal class PicassoWithHeader {

    companion object {

        fun get(context: Context): Picasso {
            val okHBuilder = OkHttpClient.Builder()
            okHBuilder.cache(OkHttp3Downloader.createDefaultCache(context))
            okHBuilder.addInterceptor(CustomInterceptorPicasso(context))

            val okHttp3Downloader = OkHttp3Downloader(okHBuilder.build())

            return Picasso.Builder(context)
                .downloader(okHttp3Downloader)
                .build()
        }
    }
}