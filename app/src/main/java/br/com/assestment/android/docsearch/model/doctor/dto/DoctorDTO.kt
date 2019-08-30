package br.com.assestment.android.docsearch.model.doctor.dto

data class DoctorDTO(
    var address: String,
    var email: String,
    var highlighted: Boolean,
    var id: String,
    var integration: String,
    var lat: Double,
    var lng: Double,
    var name: String,
    var photoId: String
)