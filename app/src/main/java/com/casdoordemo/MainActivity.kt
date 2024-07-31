package com.casdoordemo

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.casdoordemo.casdoor.Casdoor
import com.casdoordemo.casdoor.CasdoorConfig

class MainActivity : AppCompatActivity() {
    private var casdoor: Casdoor? = null
    private var isLogin = false
    private var acToken = ""
    lateinit var tvLogin:TextView
    lateinit var tvName:TextView
    lateinit var pbProgress:ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        tvLogin = findViewById(R.id.tv_login)
        tvName = findViewById(R.id.tv_name)
        pbProgress = findViewById(R.id.pb_progress)
        val casdoorConfig = CasdoorConfig(
            endpoint = "https://qa-login.safefamilyapp.com",
            clientID = "9644ff9618d6e15ca5fe",
            organizationName = "krispcall",
            redirectUri = "casdoor://callback",
            appName = "krispcall"
        )
        casdoor = Casdoor(casdoorConfig)
        val resultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult(),
            launcherCallback
        )
        tvLogin.setOnClickListener {
            if (!isLogin) {
                Log.d("this--->", casdoor?.getSignInUrl(scope = "profile").toString())
                val intent = Intent(this@MainActivity, WebViewActivity::class.java)
                intent.putExtra("url", casdoor?.getSignInUrl(scope = "profile"));
                resultLauncher.launch(intent)
            } else {
                pbProgress.visibility = View.VISIBLE
                Thread {
                    try {
                        Log.d("this->logout-1", acToken)
                        val logout = casdoor?.logout(acToken, null)
                        Log.d("this->logout-2", logout.toString())
                        if (logout == true) {
                            runOnUiThread {
                                tvName.text = ""
                                tvName.visibility = View.GONE
                                isLogin = false
                                tvName.text = "Login with Casdoor"
                                pbProgress.visibility = View.GONE
                            }
                        }
                    } catch (e: Exception) {
                        runOnUiThread {
                            pbProgress.visibility = View.GONE
                            tvName?.text = e.message
                        }
                    }
                }.start()
            }
        }

    }

    private val launcherCallback = ActivityResultCallback<ActivityResult> { result ->
        val data = result.data
        Log.d("callback-->1", result.toString())
        if (result.resultCode == RESULT_OK) {
            Log.d("callback-->", data?.data.toString())
            val code = data?.getStringExtra("code")
            pbProgress.visibility = View.VISIBLE
            Thread {
                runOnUiThread {
                    tvName?.text = "Loading..."
                }
                code?.let {
                    try {
                        acToken = casdoor?.requestOauthAccessToken(code)?.accessToken.toString()
                        Log.d("this--->acToken", acToken)
                        val userData = casdoor?.getUserInfo(acToken)
                        Log.d("this--->userData", userData.toString())

                        runOnUiThread {
                            tvName.text = userData?.name
                            tvName.visibility = View.VISIBLE
                            isLogin = true
                            tvLogin.text = "Logout"
                            pbProgress.visibility = View.GONE
                        }
                    } catch (e: Exception) {
                        runOnUiThread {
                            pbProgress.visibility = View.GONE
                            tvName?.text = e.message
                        }
                    }
                }
            }.start()

        }
    }


}