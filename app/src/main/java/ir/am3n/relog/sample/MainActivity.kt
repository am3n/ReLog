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
        }

    }

}