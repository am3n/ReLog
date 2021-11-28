package ir.am3n.relog.sample

import android.app.Application
import ir.am3n.relog.RL

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        RL.init(this, "http://192.168.1.2:7000", "...", true)

        RL.firebaseToken = "_fb_token_"

        RL.identification = "user0"

        RL.extraInfo = "09101234567"

    }

}