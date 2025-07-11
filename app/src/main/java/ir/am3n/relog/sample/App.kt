package ir.am3n.relog.sample

import android.app.Application
import ir.am3n.needtool.deviceId
import ir.am3n.relog.RL

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        RL.init(
            application = this,
            host = "http://0.0.0.0:8080",
            appKey = "APP_KEY",
            clientId = deviceId() // Custom unique device identifier (Default random)
        )

        /**
         Optional specifications
         */
        // Firebase token
        RL.firebaseToken = "USER_APP_FIREBASE_TOKEN"
        // Custom unique user identifier
        RL.identification = "user0"
        // Extra user info
        RL.extraInfo = "09101234567"

    }

}