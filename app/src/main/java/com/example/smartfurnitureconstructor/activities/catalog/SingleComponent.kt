package com.example.smartfurnitureconstructor.activities.catalog

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.widget.*
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
import kotlinx.android.synthetic.main.fragment_shop.view.*
import kotlinx.android.synthetic.main.fragment_single_report.view.*
import org.json.JSONArray
import org.json.JSONObject
import java.net.URL
import java.util.*
import kotlin.collections.HashMap
import kotlin.collections.set
import kotlin.properties.Delegates


class SingleComponent : ActivityWithTopMenu() {
    private var componentId by Delegates.notNull<Int>()

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
        componentId = intent.getIntExtra("componentId", 0)
        button3.visibility = GONE

        setListeners(buttonDescription, expandableDescriptionView)
        setListeners(buttonCharacteristics, expandableCharacteristicsView)
        setListeners(buttonShops, expandableShopsView)
        setListeners(buttonReports, expandableReportsView)
        setListeners(buttonRelatedConstructions, expandableRelatedConstructionsView)

        getComponentData()
        getComponentShops()
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
        val url = "http://192.168.0.104:9595/api/component/$componentId/"

        val queue = Volley.newRequestQueue(this)
        val req = JsonObjectRequest(Request.Method.GET, url, null,
            Response.Listener { response ->
                textView13.text = response.getString("name")
                val url: URL = URL(response.getString("image"))
                Picasso.get().load(url.toString()).into(imageView2)
                textView19.text = response.getString("description")
                ratingBar.rating = response.getInt("rating").toFloat() / 2
                getRelatedImages()
                val listView = findViewById<ListView>(R.id.listView)
                val componentWeight =
                    if (currentLocale == "en")
                        ((response.getString("weight").toFloat() / 4.5359237)
                            .toInt().toFloat() / 100).toString()
                    else
                        response.getString("weight").toString()
                val adapter = ArrayAdapter(this,
                    android.R.layout.simple_list_item_1,
                    arrayListOf(
                        resources.getString(R.string.catalog_item_manufacturer) +
                                " " + response.getString("manufacturer"),
                        resources.getString(R.string.catalog_item_category) +
                                " " + response.getString("category"),
                        resources.getString(R.string.catalog_item_type) +
                                " " + response.getString("type"),
                        resources.getString(R.string.catalog_item_weight) +
                                " " + componentWeight)
                )
                listView.adapter = adapter
                val viewItem = adapter.getView(0, null, listView)
                viewItem.measure(0, 0)
                val totalHeight = viewItem.measuredHeight * 4
                val par = listView.layoutParams
                par.height = totalHeight + (listView.dividerHeight * (adapter.count - 1))
                listView.layoutParams = par
                listView.requestLayout()


            }, Response.ErrorListener {
            })
        queue.add(req)
    }

    fun getRelatedImages() {
        val url = "http://192.168.0.104:9595/api/component_construction/$componentId/"

        val queue = Volley.newRequestQueue(this)
        val req = JsonObjectRequest(Request.Method.GET, url, null,
            Response.Listener { response ->
                val imageList = ArrayList<SlideModel>()
                for (construction in response.keys()) {
                    val constructionName: String = construction
                    val constructionImage = "http://192.168.0.104:9595/images/" +
                            (response[construction] as JSONArray).getString(0)
                    imageList.add(SlideModel(constructionImage, constructionName, ScaleTypes.CENTER_INSIDE))
                }
                val imageSlider = findViewById<ImageSlider>(R.id.image_slider)
                imageSlider.setImageList(imageList)
            }, Response.ErrorListener {
            })
        queue.add(req)
    }

    private fun getComponentShops() {
        val url = "http://192.168.0.104:9595/api/component_shops/$componentId/"

        val queue = Volley.newRequestQueue(this)
        val req = JsonObjectRequest(Request.Method.GET, url, null,
            Response.Listener { response ->
                val displayedShops: LinearLayout = shops_container
                val inflater: LayoutInflater = LayoutInflater.from(this)
                var currentShop: JSONArray = JSONArray()
                for (shop in response.keys()) {
                    val view = inflater.inflate(
                        R.layout.fragment_shop,
                        displayedShops, false)
                    currentShop = response[shop] as JSONArray
                    val url: URL = URL("http://192.168.0.104:9595/images/" +
                            currentShop.getString(0))
                    Picasso.get().load(url.toString()).into(view.imageView4)
                    view.textView12.text = shop
                    view.textView17.text =
                        currentShop.getDouble(2).toString() + " " +
                                resources.getString(R.string.cart_currency)
                    view.textView20.setOnClickListener {
                        val browser = Intent(Intent.ACTION_VIEW,
                            Uri.parse(currentShop.getString(1)))
                        startActivity(browser)
                    }
                    view.textView21.setOnClickListener {
                        val browser = Intent(Intent.ACTION_VIEW,
                            Uri.parse(currentShop.getString(3)))
                        startActivity(browser)
                    }
                    displayedShops.addView(view)
                }
            }, Response.ErrorListener {
            })
        queue.add(req)
    }

    fun getComponentReports() {
        val url = "http://192.168.0.104:9595/api/all_component_reports/$componentId/"

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
                    var date = currentReport.getString(1)
                    val year = date.substring(0, 4)
                    val month = date.substring(5, 7)
                    val day = date.substring(8, 10)
                    date = if (currentLocale == "en")
                        "$month/$day/$year"
                    else
                        "$day.$month.$year"
                    view.textView28.text = date
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
        val url = "http://192.168.0.104:9595/api/add_report_about_component/"

        val params = HashMap<String,String>()
        params["component"] = componentId.toString()
        params["text"] = view.text.toString()
        val jsonObject = JSONObject(params as Map<*, *>)

        val queue = Volley.newRequestQueue(this)
        val req : JsonObjectRequest = object : JsonObjectRequest(Request.Method.POST, url, jsonObject,
            Response.Listener { _ ->
                component_report_container.removeAllViews()
                getComponentReports()
            },  Response.ErrorListener { error -> Log.e("TAG", error.message, error) }) {
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

