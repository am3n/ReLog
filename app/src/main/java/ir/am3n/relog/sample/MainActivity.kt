package ir.am3n.relog.sample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import ir.am3n.relog.RL
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btn?.setOnClickListener {
            RL.d("MainAct", "log in debug type")
            RL.i("MainAct", "log in info type")
            RL.v("MainAct", "log in verbose type")
            RL.w("MainAct", "log in warning type")
            RL.e("MainAct", "log in error type")
            for (i in 0..2000) {
                RL.d("MainAct", "log $i in debug type to test upload huge logs to the server cause error... " +
                    "pio pio pio pio pio pio pio pio pio pio pio pio pio pio pio pio pio pio pio pio pio pio " +
                    "pio pio pio pio pio pio pio pio pio pio pio pio pio pio pio pio pio pio pio pio pio pio " +
                    "pio pio pio pio pio pio pio pio pio pio pio pio pio pio pio pio pio pio pio pio pio pio " +
                    "pio pio pio pio pio pio pio pio pio pio pio pio pio pio pio pio pio pio pio pio pio pio ")
            }
        }

    }

}