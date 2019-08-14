package br.com.assestment.android.docsearch.auth

import android.accounts.AbstractAccountAuthenticator
import android.accounts.Account
import android.accounts.AccountAuthenticatorResponse
import android.accounts.AccountManager
import android.content.Context
import android.os.Bundle
import android.text.TextUtils

class AccountAuthenticator(context: Context) : AbstractAccountAuthenticator(context) {
    lateinit var mContext: Context

    companion object {
        val ACCOUNT_TYPE = "br.com.assestment.android.docsearch"
        val TOKEN_TYPE_ACCESS = "access_token"
        val TOKEN_TYPE_REFRESH = "refresh_token"
    }

    override fun getAuthTokenLabel(authTokenType: String?): String? {
        return null
    }

    override fun confirmCredentials(
        response: AccountAuthenticatorResponse?,
        account: Account?,
        options: Bundle?
    ): Bundle? {
        return null
    }

    override fun updateCredentials(
        response: AccountAuthenticatorResponse?,
        account: Account?,
        authTokenType: String?,
        options: Bundle?
    ): Bundle? {
        return null
    }

    override fun getAuthToken(
        response: AccountAuthenticatorResponse?,
        account: Account?,
        authTokenType: String?,
        options: Bundle?
    ): Bundle? {
        val result = Bundle()
        // get account manager instance
        val accountManager = AccountManager.get(mContext)
        // get a cached token or null if no one exists
        val authToken = accountManager.peekAuthToken(account, authTokenType)
        // if a token was caught, return it
        if (!TextUtils.isEmpty(authToken)) {
            result.putString(AccountManager.KEY_AUTHTOKEN, authToken)
            result.putString(AccountManager.KEY_ACCOUNT_NAME, account?.name)
            result.putString(AccountManager.KEY_ACCOUNT_TYPE, account?.type)
            response?.onResult(result)
        } else {
            //doAuthenticationRefreshRequest
        }

        return null
    }

    override fun hasFeatures(
        response: AccountAuthenticatorResponse?,
        account: Account?,
        features: Array<out String>?
    ): Bundle? {
        return null
    }

    override fun editProperties(response: AccountAuthenticatorResponse?, accountType: String?): Bundle? {
        return null
    }

    override fun addAccount(
        response: AccountAuthenticatorResponse?,
        accountType: String?,
        authTokenType: String?,
        requiredFeatures: Array<out String>?,
        options: Bundle?
    ): Bundle? {
        return null
    }
}