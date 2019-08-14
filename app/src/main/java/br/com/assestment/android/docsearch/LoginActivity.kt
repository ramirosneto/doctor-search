package br.com.assestment.android.docsearch

import android.accounts.Account
import android.accounts.AccountManager
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Patterns
import android.view.View
import android.widget.Toast
import br.com.assestment.android.docsearch.auth.AccountAuthenticator
import br.com.assestment.android.docsearch.model.auth.dto.TokenResponse
import br.com.assestment.android.docsearch.services.ServicesHelper
import br.com.assestment.android.docsearch.services.`interface`.AccessTokenServiceInterface
import kotlinx.android.synthetic.main.activity_login.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (userIsLogged()) {
            startActivity(SearchActivity.intentToShow(applicationContext))
            finish()
        } else {
            setContentView(R.layout.activity_login)
            setListeners()
        }
    }

    private fun userIsLogged(): Boolean {
        val accountType = AccountAuthenticator.ACCOUNT_TYPE
        val am = AccountManager.get(applicationContext)
        val sbAccounts = am.getAccountsByType(accountType)
        if (sbAccounts.size > 0 && sbAccounts[0].type.equals(accountType, ignoreCase = true)) {
            return true
        }

        return false
    }

    private fun setListeners() {
        btn_login.setOnClickListener {
            if (validateFields()) {
                btn_login.visibility = View.GONE
                loading.visibility = View.VISIBLE

                val username = username.text.toString()
                val password = password.text.toString()
                val authorization = "Basic aXBob25lOmlwaG9uZXdpbGxub3RiZXRoZXJlYW55bW9yZQ=="
                val retrofitClient = ServicesHelper.client(true)
                val endpoint = retrofitClient?.create(AccessTokenServiceInterface::class.java)
                val callback = endpoint?.getAccessToken(
                    "password",
                    username, password, authorization
                )

                callback?.enqueue(object : Callback<TokenResponse?> {
                    override fun onFailure(call: Call<TokenResponse?>, t: Throwable) {
                        handleLoginError(t)
                    }

                    override fun onResponse(call: Call<TokenResponse?>, response: Response<TokenResponse?>) {
                        handleLoginSuccess(response.body())
                    }
                })
            }
        }
    }

    private fun handleLoginSuccess(response: TokenResponse?) {
        val account = Account(getString(R.string.app_name), AccountAuthenticator.ACCOUNT_TYPE)
        val accountManager = AccountManager.get(applicationContext)
        val accountCreated = accountManager.addAccountExplicitly(account, null, null)

        if (accountCreated) {
            accountManager.setAuthToken(account, AccountAuthenticator.TOKEN_TYPE_ACCESS, response?.access_token)
            accountManager.setAuthToken(account, AccountAuthenticator.TOKEN_TYPE_REFRESH, response?.refresh_token)
        }

        startActivity(SearchActivity.intentToShow(applicationContext))
        finish()
    }

    private fun handleLoginError(t: Throwable) {
        loading.visibility = View.GONE
        btn_login.visibility = View.VISIBLE

        Toast.makeText(applicationContext, R.string.auth_service_error, Toast.LENGTH_LONG).show()
    }

    private fun validateFields(): Boolean {
        if (!Patterns.EMAIL_ADDRESS.matcher(username.text.toString()).matches()) {
            til_username.isErrorEnabled = true
            til_username.error = getString(R.string.invalid_email)
            return false;
        } else {
            til_username.isErrorEnabled = false
            til_username.error = null
        }

        if (password.length() < 3) {
            til_password.isErrorEnabled = true
            til_password.error = getString(R.string.invalid_email)
            return false
        } else {
            til_password.isErrorEnabled = false
            til_password.error = null
        }

        return true
    }
}