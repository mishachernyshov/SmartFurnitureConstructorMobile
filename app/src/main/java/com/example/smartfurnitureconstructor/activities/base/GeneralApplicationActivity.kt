package com.example.smartfurnitureconstructor.activities.base

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.example.smartfurnitureconstructor.R
import org.json.JSONArray
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList
import kotlin.properties.Delegates


open class GeneralApplicationActivity : AppCompatActivity() {
    lateinit var currentLocale: String
    var activityMenu by Delegates.notNull<Int>()
    lateinit var requestQueue: RequestQueue

    override fun onResume() {
        val locale = resources.configuration.locale.toString()
        if (locale != currentLocale) {
            finish()
            startActivity(intent)
        }
        super.onResume()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(activityMenu, menu)
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityMenu = R.menu.change_language_menu
        currentLocale = resources.configuration.locale.toString()
    }

    fun changeUserInterfaceLanguage() {
        currentLocale = if (currentLocale == "en") "uk" else "en"
        val locale = Locale(currentLocale)
        Locale.setDefault(locale)
        val resources: Resources = this.resources
        val config: Configuration = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
        finish()
        startActivity(intent)
    }

    fun eraseAccessToken() {
        val prefs = getSharedPreferences("authPrefs", Context.MODE_PRIVATE)
        val edit = prefs.edit()
        edit.putString("access_token", "")
        edit.apply()
    }

    fun getJwtHeaderParams(): MutableMap<String, String> {
        val params: MutableMap<String, String> =
            HashMap()
        val prefs = getSharedPreferences("authPrefs", Context.MODE_PRIVATE)
        val accessToken = "JWT " +
                prefs.getString("access_token", "").toString()
        params["Authorization"] = accessToken
        return params
    }

    private fun sendJsonObjectRequestWithoutHeader(
        url: String,
        queue: RequestQueue?,
        method: Int,
        requestBody: JSONObject?,
        successFunction: (Any, ArrayList<Any?>) -> Unit,
        successParams: ArrayList<Any?>,
        failFunction: (VolleyError, ArrayList<Any?>) -> Unit,
        failParams: ArrayList<Any?>) {

        val req = JsonObjectRequest(method, url, requestBody,
            Response.Listener { response ->
                successFunction(response as Any, successParams)
            }, Response.ErrorListener { error ->
                failFunction(error, failParams)
            })
        queue?.add(req)
    }

    private fun sendJsonArrayRequestWithoutHeader(
        url: String,
        queue: RequestQueue?,
        method: Int,
        requestBody: JSONArray?,
        successFunction: (Any, ArrayList<Any?>) -> Unit,
        successParams: ArrayList<Any?>,
        failFunction: (VolleyError, ArrayList<Any?>) -> Unit,
        failParams: ArrayList<Any?>) {

        val req = JsonArrayRequest(method, url, requestBody,
            Response.Listener { response ->
                successFunction(response as Any, successParams)
            }, Response.ErrorListener { error ->
                failFunction(error, failParams)
            })
        queue?.add(req)
    }

    fun sendPostRequestWithoutHeader(
        url: String,
        params: HashMap<String,String>,
        queue: RequestQueue?,
        successFunction: (Any, ArrayList<Any?>) -> Unit,
        successParams: ArrayList<Any?>,
        failFunction: (VolleyError, ArrayList<Any?>) -> Unit,
        failParams: ArrayList<Any?>) {

        val jsonObject = JSONObject(params as Map<*, *>)

        sendJsonObjectRequestWithoutHeader(url, queue, Request.Method.POST,
            jsonObject, successFunction, successParams, failFunction, failParams)
    }

    fun sendGetRequestWithoutHeader(
        url: String,
        queue: RequestQueue?,
        successFunction: (Any, ArrayList<Any?>) -> Unit,
        successParams: ArrayList<Any?>,
        failFunction: (VolleyError, ArrayList<Any?>) -> Unit,
        failParams: ArrayList<Any?>) {

        sendJsonObjectRequestWithoutHeader(url, queue, Request.Method.GET,
            null, successFunction, successParams, failFunction, failParams)
    }

    fun sendJsonArrayGetRequestWithoutHeader(
        url: String,
        queue: RequestQueue?,
        successFunction: (Any, ArrayList<Any?>) -> Unit,
        successParams: ArrayList<Any?>,
        failFunction: (VolleyError, ArrayList<Any?>) -> Unit,
        failParams: ArrayList<Any?>) {

        sendJsonArrayRequestWithoutHeader(url, queue, Request.Method.GET,
            null, successFunction, successParams, failFunction, failParams)
    }

    fun startNewActivity(cls: Class<*>) {
        val componentCatalogIntent = Intent(this, cls)
        startActivity(componentCatalogIntent)
    }
}

