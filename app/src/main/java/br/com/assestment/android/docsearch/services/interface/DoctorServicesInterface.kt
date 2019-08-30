package br.com.assestment.android.docsearch.services.`interface`

import br.com.assestment.android.docsearch.model.doctor.dto.SearchDoctor
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface DoctorServicesInterface {

    @GET("users/me/doctors")
    fun search(
        @Query("search") search: String?,
        @Query("lat") lat: Double,
        @Query("lng") lng: Double,
        @Query("lastKey") lastKey: String?,
        @Header("Authorization") authorization: String
    ): Call<SearchDoctor>
}