package ir.am3n.relog.sample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.AppCompatButton
import ir.am3n.relog.RL

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        RL.d("MainAct", "onCreate")

        val btn = findViewById<AppCompatButton>(R.id.btn)
        btn?.setOnClickListener {
            RL.d("MainAct", "button click")
            /*RL.i("MainAct", "log in info type")
            RL.v("MainAct", "log in verbose type")
            RL.w("MainAct", "log in warning type")
            RL.e("MainAct", "log in error type")*/
        }

        /*for (i in 0..50024) {
            RL.d("MainAct", "log in debug type, log in debug type, log in debug type, log in debug type, " +
                    "log in debug type, log in debug type, log in debug type, log in debug type, " +
                    "log in debug type, log in debug type, log in debug type, log in debug type, " +
                    "log in debug type, log in debug type, log in debug type, log in debug type, " +
                    "log in debug type, log in debug type, ")
        }*/

    }

    override fun onStop() {
        super.onStop()
        RL.d("MainAct", "onStop")
    }

}