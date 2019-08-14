package br.com.assestment.android.docsearch.model.doctor.dto

data class SearchDoctor (
    var doctors: ArrayList<DoctorDTO>,
    var lastKey: String
)