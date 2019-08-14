package br.com.assestment.android.docsearch.services.`interface`

import br.com.assestment.android.docsearch.model.auth.dto.TokenResponse
import retrofit2.Call
import retrofit2.http.*

interface AccessTokenServiceInterface {

    @POST("oauth/token")
    fun getAccessToken(
        @Query("grant_type") grant_type: String,
        @Query("username") username: String,
        @Query("password") password: String,
        @Header("Authorization") authorization: String
    ): Call<TokenResponse>
}