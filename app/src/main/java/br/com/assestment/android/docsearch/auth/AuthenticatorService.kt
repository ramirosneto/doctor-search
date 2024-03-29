package br.com.assestment.android.docsearch.auth

import android.app.Service
import android.content.Intent
import android.os.IBinder

class AuthenticatorService : Service() {

    override fun onBind(intent: Intent): IBinder? {
        val authenticator = AccountAuthenticator(this)
        return authenticator.getIBinder()
    }
}