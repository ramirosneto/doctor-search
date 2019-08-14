package br.com.assestment.android.docsearch.services.`interface`

import br.com.assestment.android.docsearch.model.doctor.dto.SearchDoctor
import retrofit2.Call
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface SearchDoctorServiceInterface {

    @POST("users/me/doctors")
    fun searchDoctor(
        @Query("search") search: String,
        @Query("lat") lat: Double,
        @Query("lng") lng: Double,
        @Header("Authorization") authorization: String
    ): Call<SearchDoctor>
}