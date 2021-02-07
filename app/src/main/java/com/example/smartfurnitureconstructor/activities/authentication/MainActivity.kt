package com.example.smartfurnitureconstructor.activities.authentication

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import com.android.volley.VolleyError
import com.android.volley.toolbox.Volley
import com.example.smartfurnitureconstructor.R
import com.example.smartfurnitureconstructor.activities.base.ActivityWithoutTopMenu
import com.example.smartfurnitureconstructor.activities.catalog.ComponentCatalog
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class MainActivity : ActivityWithoutTopMenu() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestQueue = Volley.newRequestQueue(this)
        startTokenVerification()
    }

    override fun onResume() {
        super.onResume()
        startTokenVerification()
    }

    private fun startTokenVerification() {
        val prefs = getSharedPreferences("authPrefs", Context.MODE_PRIVATE)
        val accessToken = prefs.getString("access_token", "").toString()
        verifyAccessToken(prefs, null, accessToken)
    }

    fun sendGet(view: View) {
        val prefs = getSharedPreferences("authPrefs", Context.MODE_PRIVATE)
        val edit = prefs.edit()
/*        edit.putString("access_token", "")
        edit.commit()*/

        val accessToken = prefs.getString("access_token", "").toString()

        if (accessToken == "") {
            authorize(prefs, edit)
        } else {
            verifyAccessToken(prefs, edit, accessToken)
        }
    }

    private fun authorize(prefs: SharedPreferences,
                          edit: SharedPreferences.Editor) {

        val url = "http://192.168.0.104:9595/auth/jwt/create/"
        val params = HashMap<String,String>()
        params["username"] = editTextTextPersonName.text.toString()
        params["password"] = editTextTextPassword.text.toString()
        val queue = Volley.newRequestQueue(this)

        sendPostRequestWithoutHeader(url, params, requestQueue,
            this::executeIfAuthorizationIsSuccessful,
            arrayListOf(prefs, edit),
            this::executeIfAuthorizationIsFailed, arrayListOf())
    }

    private fun executeIfAuthorizationIsSuccessful(
        response: Any,
        args: ArrayList<Any?>) {
        saveNewTokens(response, args)
    }

    private fun executeIfAuthorizationIsFailed(
        error: VolleyError,
        args: ArrayList<Any?>) {
        //textView53.visibility = VISIBLE
    }

    private fun saveNewTokens(response: Any,
                              args: ArrayList<Any?>) {
        val prefs: SharedPreferences = args[0] as SharedPreferences
        val edit: SharedPreferences.Editor = args[1] as SharedPreferences.Editor
        val responseJsonObject = response as JSONObject
        edit.putString("access_token", responseJsonObject["access"].toString())
        edit.putString("refresh_token", responseJsonObject["refresh"].toString())
        edit.commit()
        verifyAccessToken(prefs, edit, responseJsonObject["access"].toString())
    }

    private fun verifyAccessToken(prefs: SharedPreferences,
                                  edit: SharedPreferences.Editor?,
                                  accessToken: String) {

        val url = "http://192.168.0.104:9595/auth/jwt/verify/"
        val params = HashMap<String,String>()
        params["token"] = accessToken
        val queue = Volley.newRequestQueue(this)

        sendPostRequestWithoutHeader(url, params, requestQueue,
            this::executeIfTokenVerificationIsSuccessful,
            arrayListOf(),
            this::executeIfTokenVerificationIsFailed,
            arrayListOf(prefs, edit, accessToken))
    }

    private fun executeIfTokenVerificationIsSuccessful(
        response: Any,
        args: ArrayList<Any?>
    ) {
        openComponentCatalog()
    }

    private fun executeIfTokenVerificationIsFailed(
        error: VolleyError,
        args: ArrayList<Any?>) {
        if (args[1] != null) {
            refreshJWT(args[0] as SharedPreferences,
                args[1] as SharedPreferences.Editor?,
                args[2] as String
            )
        }
    }

    private fun refreshJWT(prefs: SharedPreferences,
                           edit: SharedPreferences.Editor?,
                           accessToken: String) {

        val url = "http://192.168.0.104:9595/auth/jwt/refresh/"
        val params = HashMap<String,String>()
        params["refresh"] = prefs.getString(
            "refresh_token", "").toString()
        val queue = Volley.newRequestQueue(this)

        sendPostRequestWithoutHeader(url, params, requestQueue,
            this::executeIfJwtRefreshIsSuccessful,
            arrayListOf(prefs, edit, accessToken),
            this::executeIfJwtRefreshIsFailed,
            arrayListOf())
    }

    private fun executeIfJwtRefreshIsSuccessful(
        response: Any,
        args: ArrayList<Any?>) {
        setNewAccessTokenValue(response as JSONObject, args)
    }

    private fun executeIfJwtRefreshIsFailed(
        error: VolleyError,
        args: ArrayList<Any?>) {}

    private fun setNewAccessTokenValue(response: JSONObject,
                               args: ArrayList<Any?>) {
        val prefs = args[0] as SharedPreferences
        val edit = args[1] as SharedPreferences.Editor?
        val accessToken = args[2] as String
        edit?.putString("access_token", response["access"].toString())
        edit?.commit()
        verifyAccessToken(prefs, edit, accessToken)
    }

    fun openRegistrationForm(view: View) {
        startNewActivity(RegistrationActivity::class.java)
    }

    private fun openComponentCatalog() {
        textView53.visibility = INVISIBLE
        startNewActivity(ComponentCatalog::class.java)
    }
}