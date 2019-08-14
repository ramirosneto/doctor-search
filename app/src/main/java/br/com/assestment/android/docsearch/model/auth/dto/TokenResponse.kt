package br.com.assestment.android.docsearch.model.auth.dto

data class TokenResponse(
    var access_token: String,
    var token_type: String,
    var refresh_token: String,
    var expires_in: Int,
    var scope: String,
    var jti: String,
    var phoneVerified: Boolean
)