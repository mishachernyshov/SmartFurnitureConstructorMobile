package com.example.smartfurnitureconstructor.activities.authentication

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.smartfurnitureconstructor.R
import com.example.smartfurnitureconstructor.activities.base.ActivityWithoutTopMenu
import kotlinx.android.synthetic.main.activity_registration.*
import org.json.JSONObject
import kotlin.collections.HashMap

class RegistrationActivity : ActivityWithoutTopMenu() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)
    }

    fun registerNewUser(view: View) {
        val url = "http://192.168.0.104:9595/auth/users/"

        val params = HashMap<String,String>()
        params["username"] = editTextTextPersonName2.text.toString()
        params["password"] = editTextTextPassword2.text.toString()
        params["re_password"] = editTextTextPassword3.text.toString()
        val jsonObject = JSONObject(params as Map<*, *>)

        val queue = Volley.newRequestQueue(this)
        val req = JsonObjectRequest(Request.Method.POST, url, jsonObject,
            Response.Listener { response ->
                val successRegistration = AlertDialog.Builder(this)
                successRegistration.setMessage("Ви були успішно зареєстровані")
                    .setCancelable(false).setPositiveButton("Ok") { _, _ ->
                        run {
                            val authorizationIntent = Intent(
                                this,
                                MainActivity::class.java
                            )
                            startActivity(authorizationIntent)
                        }
                    }
                val alertDialog = successRegistration.create()
                alertDialog.setTitle("Реєстрація")
                alertDialog.show()
            }, Response.ErrorListener {
                textView55.visibility = View.VISIBLE
            })
        queue.add(req)
    }

    fun openAuthorizationForm(view: View) {
        textView55.visibility = View.INVISIBLE
        val authorizationIntent = Intent(this,
            MainActivity::class.java)
        startActivity(authorizationIntent)
    }
}