package com.example.smartfurnitureconstructor.activities.functional

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.smartfurnitureconstructor.R
import com.example.smartfurnitureconstructor.activities.base.ActivityWithTopMenu
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_bought_instructions.*
import kotlinx.android.synthetic.main.fragment_single_bought_instruction.view.*
import org.json.JSONArray
import java.net.URL

class BoughtInstructions : ActivityWithTopMenu() {
    private val userConstructions: ArrayList<Triple<String, String, Int>> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bought_instructions)

        getUserConstructions()
    }

    private fun getUserConstructions() {
        val url = "http://192.168.0.104:9595/api/user_construction_list/"

        val queue = Volley.newRequestQueue(this)
        val req : JsonObjectRequest =
            object : JsonObjectRequest(Request.Method.GET, url, null,
                Response.Listener { response ->
                    val displayedConstructions: LinearLayout = boughtConstructionContainer
                    val inflater: LayoutInflater = LayoutInflater.from(this)
                    for (construction in response.keys()) {
                        val view = inflater.inflate(
                            R.layout.fragment_single_bought_instruction,
                            displayedConstructions, false)
                        val url: URL = URL("http://192.168.0.104:9595/images/" +
                                (response[construction] as JSONArray).getString(0))
                        Picasso.get().load(url.toString()).into(view.imageView6)
                        view.textView48.text = construction
                        view.button8.tag = (response[construction] as JSONArray).getInt(1)
                        displayedConstructions.addView(view)
                    }
                }, Response.ErrorListener {
                }) {
                @Throws(AuthFailureError::class)
                override fun getHeaders(): Map<String, String> {
                    val params: MutableMap<String, String> =
                        HashMap()
                    val prefs = getSharedPreferences("authPrefs", Context.MODE_PRIVATE)
                    val accessToken = "JWT " +
                            prefs.getString("access_token", "").toString()
                    params["Authorization"] = accessToken
                    return params
                }
            }
        queue.add(req)
    }

    fun openSingleBoughtConstruction(view: View) {
        val constructionIntent = Intent(this,
            QR_Scanner::class.java)
        constructionIntent.putExtra("constructionId", view.tag as Int)
        startActivity(constructionIntent)
    }
}