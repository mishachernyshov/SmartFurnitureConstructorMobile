package com.example.smartfurnitureconstructor.activities.catalog

import android.content.Context
import android.os.Bundle
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.denzcoskun.imageslider.ImageSlider
import com.denzcoskun.imageslider.constants.ScaleTypes
import com.denzcoskun.imageslider.models.SlideModel
import com.example.smartfurnitureconstructor.R
import com.example.smartfurnitureconstructor.activities.base.ActivityWithTopMenu
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_single_componen_screen.*
import kotlinx.android.synthetic.main.fragment_report_sending.view.*
import kotlinx.android.synthetic.main.fragment_single_report.view.*
import org.json.JSONArray
import org.json.JSONObject
import java.net.URL
import java.util.*
import kotlin.collections.HashMap
import kotlin.collections.set
import kotlin.properties.Delegates


class SingleConstruction : ActivityWithTopMenu() {

    var relatedConstructions: ArrayList<Pair<String, String>> = arrayListOf()
    private var constructionId by Delegates.notNull<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_single_componen_screen)

        val expandableDescriptionView: ConstraintLayout = expandable_constraint_description
        val buttonDescription: Button = description_arrow_button
        val expandableCharacteristicsView: ConstraintLayout = expandable_constraint_characteristics
        val buttonCharacteristics: Button = characteristics_arrow_button
        val expandableShopsView: ConstraintLayout = expandable_constraint_shops
        val buttonShops: Button = shops_arrow_button
        val expandableReportsView: ConstraintLayout = expandable_constraint_reports
        val buttonReports: Button = reports_arrow_button
        val expandableRelatedConstructionsView: ConstraintLayout =
            expandable_constraint_related_construction
        val buttonRelatedConstructions: Button = related_construction_arrow_button
        val currentIntent = intent
        constructionId = intent.getIntExtra("constructionId", 0)
        component_shops_card.visibility = GONE
        component_characteristics_card.visibility = GONE
        textView26.text = resources.getString(R.string.catalog_item_related_components)

        setListeners(buttonDescription, expandableDescriptionView)
        setListeners(buttonCharacteristics, expandableCharacteristicsView)
        setListeners(buttonShops, expandableShopsView)
        setListeners(buttonReports, expandableReportsView)
        setListeners(buttonRelatedConstructions, expandableRelatedConstructionsView)

        getComponentData()
        getComponentReports()
    }

    fun setListeners(button: Button, expandedView: ConstraintLayout) {
        button.setOnClickListener {
            if (expandedView.visibility == View.GONE) {
                TransitionManager.beginDelayedTransition(
                    component_description_card, AutoTransition())
                expandedView.visibility = View.VISIBLE
                button.setBackgroundResource(R.drawable.ic_baseline_keyboard_arrow_up_24)
            } else {
                TransitionManager.beginDelayedTransition(
                    component_description_card, AutoTransition())
                expandedView.visibility = View.GONE
                button.setBackgroundResource(R.drawable.ic_baseline_keyboard_arrow_down_24)
            }
        }
    }

    fun getComponentData() {
        val url = "http://192.168.0.104:9595/api/assembled_construction/$constructionId/"

        val queue = Volley.newRequestQueue(this)
        val req = JsonObjectRequest(Request.Method.GET, url, null,
            Response.Listener { response ->
                textView13.text = response.getString("name")
                val url: URL = URL(response.getString("image"))
                Picasso.get().load(url.toString()).into(imageView2)
                textView19.text = response.getString("description")
                ratingBar.rating = response.getInt("rating").toFloat() / 2
                getRelatedImages()
            }, Response.ErrorListener {
            })
        queue.add(req)
    }

    fun getRelatedImages() {
        val url = "http://192.168.0.104:9595/api/construction_component/$constructionId/"

        val queue = Volley.newRequestQueue(this)
        val req = JsonObjectRequest(Request.Method.GET, url, null,
            Response.Listener { response ->
                val imageList = ArrayList<SlideModel>()
                for (construction in response.keys()) {
                    val constructionName: String = construction
                    val constructionImage = "http://192.168.0.104:9595/images/" +
                            (response[construction] as JSONArray).getString(0)
                    imageList.add(SlideModel(constructionImage, constructionName, ScaleTypes.FIT))
                }
                val imageSlider = findViewById<ImageSlider>(R.id.image_slider)
                imageSlider.setImageList(imageList)
            }, Response.ErrorListener {
            })
        queue.add(req)
    }

    fun getComponentReports() {
        val url = "http://192.168.0.104:9595/api/all_construction_reports/$constructionId/"

        val queue = Volley.newRequestQueue(this)
        val req = JsonObjectRequest(Request.Method.GET, url, null,
            Response.Listener { response ->
                val displayedReports: LinearLayout = component_report_container
                val inflater: LayoutInflater = LayoutInflater.from(this)
                val reportArray = response.getJSONArray("reports")
                for (i in 0 until reportArray.length()) {
                    val view = inflater.inflate(
                        R.layout.fragment_single_report,
                        displayedReports, false)
                    val currentReport = reportArray.getJSONArray(i)
                    view.textView23.text = currentReport.getString(0)
                    view.textView28.text = currentReport.getString(1)
                    view.textView29.text = currentReport.getString(2)
                    displayedReports.addView(view)
                }
                val writeReportView = inflater.inflate(
                    R.layout.fragment_report_sending,
                    displayedReports, false)
                writeReportView.button4.setOnClickListener {
                    sendReportListener(writeReportView.editTextTextMultiLine)
                }
                displayedReports.addView(writeReportView)
            }, Response.ErrorListener {
            })
        queue.add(req)
    }

    private fun sendReportListener(view: EditText) {
        val url = "http://192.168.0.104:9595/api/add_report_about_construction/"

        val params = HashMap<String,String>()
        params["construction"] = constructionId.toString()
        params["text"] = view.text.toString()
        val jsonObject = JSONObject(params as Map<*, *>)

        val queue = Volley.newRequestQueue(this)
        val req : JsonObjectRequest = object : JsonObjectRequest(Request.Method.POST, url, jsonObject,
            Response.Listener { _ ->
                component_report_container.removeAllViews()
                getComponentReports()
            },  Response.ErrorListener { error ->
                Log.e("TAG", error.message, error) }) {
            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                val params: MutableMap<String, String> =
                    HashMap()
                val prefs = getSharedPreferences("authPrefs", Context.MODE_PRIVATE)
                val accessToken = "JWT " + prefs.getString("access_token", "").toString()
                params["Authorization"] = accessToken
                return params
            }
        }
        queue.add(req)
    }

    fun addConstructionToCartListener(view: View) {
        val url = "http://192.168.0.104:9595/api/add_to_cart/"

        val params = HashMap<String,String>()
        params["construction"] = constructionId.toString()
        val jsonObject = JSONObject(params as Map<*, *>)

        val queue = Volley.newRequestQueue(this)
        val req : JsonObjectRequest = object : JsonObjectRequest(Request.Method.POST, url, jsonObject,
            Response.Listener { _ ->
                val successBuying = AlertDialog.Builder(this)
                successBuying.setMessage(resources.getString(R.string.buy_instruction_alert))
                    .setCancelable(false).setPositiveButton("Ok") { _, _ ->
                    }
                val alertDialog = successBuying.create()
                alertDialog.setTitle("Придбання інструкції")
                alertDialog.show()
            },  Response.ErrorListener { error ->
                Log.e("TAG", error.message, error) }) {
            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                val params: MutableMap<String, String> =
                    HashMap()
                val prefs = getSharedPreferences("authPrefs", Context.MODE_PRIVATE)
                val accessToken = "JWT " + prefs.getString("access_token", "").toString()
                params["Authorization"] = accessToken
                return params
            }
        }
        queue.add(req)
    }

}

