package ir.am3n.relog.sample

import android.app.Application
import ir.am3n.relog.RL

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        RL.init(
            this,
            "http://192.168.1.101:7000",
            "8okjszce955745nb"
        )

        RL.firebaseToken = "_fb_token_"

        RL.identification = "user0"

        RL.extraInfo = "09151234567"

    }

}