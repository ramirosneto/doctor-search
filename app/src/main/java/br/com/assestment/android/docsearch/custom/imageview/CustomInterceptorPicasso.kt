package br.com.socialbank.android.socialpartner.custom.picasso

import android.accounts.Account
import android.accounts.AccountManager
import android.content.Context
import br.com.assestment.android.docsearch.R
import br.com.assestment.android.docsearch.auth.AccountAuthenticator
import okhttp3.Interceptor
import okhttp3.Response

class CustomInterceptorPicasso(context: Context) : Interceptor {
    private val mContext = context

    override fun intercept(chain: Interceptor.Chain): Response {
        val account = Account(mContext.getString(R.string.app_name), AccountAuthenticator.ACCOUNT_TYPE)
        val accountManager = AccountManager.get(mContext)
        val accessToken = accountManager.peekAuthToken(account, AccountAuthenticator.TOKEN_TYPE_ACCESS)

        val originalRequest = chain.request()
        val response = originalRequest.newBuilder()
            .addHeader("Authorization", "Bearer ".plus(accessToken))
            .build()

        return chain.proceed(response)
    }
}