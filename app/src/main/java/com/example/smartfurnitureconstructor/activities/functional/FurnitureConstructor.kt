package com.example.smartfurnitureconstructor.activities.functional

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.denzcoskun.imageslider.ImageSlider
import com.denzcoskun.imageslider.constants.ScaleTypes
import com.denzcoskun.imageslider.models.SlideModel
import com.example.smartfurnitureconstructor.R
import com.example.smartfurnitureconstructor.activities.base.ActivityWithTopMenu
import com.google.android.flexbox.FlexboxLayoutManager
import kotlinx.android.synthetic.main.activity_furniture_constructor.*
import org.json.JSONArray
import org.json.JSONObject
import java.util.*
import kotlin.concurrent.schedule


class FurnitureConstructor : ActivityWithTopMenu() {
    private val components: ArrayList<Pair<String, Int>> = arrayListOf(Pair("", -1))
    private val constructions: ArrayList<Pair<String, Int>> = arrayListOf(Pair("", -1))
    private val appropriateConstructions: ArrayList<Triple<Int, String, String>> = arrayListOf()
    private val almostAppropriateConstructions: ArrayList<Triple<Int, String, String>> = arrayListOf()
    private val colorSequence: ArrayList<Int> = arrayListOf(R.color.pink, R.color.green, R.color.blue,
        R.color.dark_blue, R.color.orange, R.color.purple)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_furniture_constructor)

        getComponents()
        getConstructions()

        spinner3.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parentView: AdapterView<*>?,
                selectedItemView: View,
                position: Int,
                id: Long
            ) {
                if (id != -1L) {
                    addComponentButton(spinner3.selectedItem.toString(), id.toInt())
                }
            }

            override fun onNothingSelected(parentView: AdapterView<*>?) {
            }
        }

        spinner2.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parentView: AdapterView<*>?,
                selectedItemView: View,
                position: Int,
                id: Long
            ) {
                if (id != -1L) {
                    getConstructionComponents(id.toInt())
                }
            }

            override fun onNothingSelected(parentView: AdapterView<*>?) {
            }
        }

        button6.setOnClickListener {
            textView35.visibility = ViewGroup.GONE
            appropriate_slider.visibility = ViewGroup.GONE
            textView38.visibility = ViewGroup.GONE
            almost_appropriate_slider.visibility = ViewGroup.GONE
            textView39.visibility = ViewGroup.GONE
            val currentComponents = getChosenComponentsId()
            getAppropriateConstructions(currentComponents)
            getAlmostAppropriateConstructions(currentComponents)

            Timer("SettingUp", false).schedule(1000) {
                if (appropriate_slider.visibility == ViewGroup.GONE &&
                    textView38.visibility == ViewGroup.GONE) {
                        textView39.visibility = ViewGroup.VISIBLE
                }
            }
        }
    }

    private fun getEntity(url: String,
                          entityArray: ArrayList<Pair<String, Int>>,
                          view: Spinner) {
        var currentObject: JSONObject

        val queue = Volley.newRequestQueue(this)
        val req = JsonArrayRequest(Request.Method.GET, url, null,
            Response.Listener { response ->
                for (i in 0 until response.length()) {
                    currentObject = response.getJSONObject(i)
                    entityArray.add(Pair(currentObject.getString("name"),
                        currentObject.getInt("id")))
                }
                val dropDownAdapter =
                    MyAdapter(
                        this,
                        android.R.layout.simple_spinner_item, entityArray
                    )
                dropDownAdapter.setDropDownViewResource(
                    android.R.layout.simple_spinner_dropdown_item)
                view.adapter = dropDownAdapter
            }, Response.ErrorListener {
            })
        queue.add(req)
    }

    private fun getComponents() {
        val url = "http://192.168.0.104:9595/api/component/"
        getEntity(url, components, spinner3)
    }

    private fun getConstructions() {
        val url = "http://192.168.0.104:9595/api/assembled_construction/"
        getEntity(url, constructions, spinner2)
    }

    private fun getConstructionComponents(constructionId: Int) {
        if (constructionId == -1) {
            return
        }

        val url = "http://192.168.0.104:9595/api/construction_component/$constructionId/"

        val queue = Volley.newRequestQueue(this)
        val req = JsonObjectRequest(Request.Method.GET, url, null,
            Response.Listener { response ->
                for (component in response.keys()) {
                    addComponentButton(component,
                        (response[component] as JSONArray).getInt(1))
                }
            }, Response.ErrorListener {
            })
        queue.add(req)
    }

    private fun addComponentButton(buttonText: String, buttonTag: Int) {
        val params: FlexboxLayoutManager.LayoutParams =
            FlexboxLayoutManager.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT)
        params.setMargins(10, 5, 10, 5)

        val newButton = TextView(applicationContext)
        newButton.layoutParams = params
        newButton.text = buttonText
        newButton.textSize = 18F
        newButton.setTextColor(resources.getColor(R.color.white))
        newButton.setPadding(30,10,30,10)
        val buttonColor: Int = colorSequence.random()
        newButton.setBackgroundResource(buttonColor)
        newButton.setOnClickListener { view ->
            val parentView = view.parent as ViewGroup
            parentView.removeView(view)
        }
        newButton.tag = buttonTag
        buttonContainer.addView(newButton)
    }

    private fun getChosenComponentsId(): ArrayList<Int> {
        val idArray: ArrayList<Int> = arrayListOf()
        var currentChild: View
        for (child in 0 until buttonContainer.childCount) {
            currentChild = buttonContainer.getChildAt(child)
            idArray.add(currentChild.tag as Int)
        }
        return idArray
    }

    private fun getConstructionsToDisplay(
        constructionsId: ArrayList<Int>,
        constructionsContainer: ArrayList<Triple<Int, String, String>>,
        url: String,
        showFunction: () -> Unit
    ) {
        val url = StringBuilder(url)
        for (k in 0 until constructionsId.size) {
            url.append("0=${constructionsId[k]}&")
        }

        constructionsContainer.clear()
        val queue = Volley.newRequestQueue(this)
        val req = JsonArrayRequest(Request.Method.GET, url.toString(), null,
            Response.Listener { response ->
                var currentConstruction: JSONObject
                for (i in 0 until response.length()) {
                    currentConstruction = response.getJSONObject(i)
                    constructionsContainer.add(
                        Triple(
                            currentConstruction.getInt("id"),
                            currentConstruction.getString("name"),
                            currentConstruction.getString("image")
                        )
                    )
                }
                if (constructionsContainer.size != 0) {
                    textView39.visibility = ViewGroup.GONE
                    showFunction()
                }
            }, Response.ErrorListener {
            })
        queue.add(req)
    }

    private fun getAppropriateConstructions(constructionsId: ArrayList<Int>) {
        getConstructionsToDisplay(constructionsId, appropriateConstructions,
            "http://192.168.0.104:9595/api/appropriate_construction/?",
            this::showAppropriateConstructions)
    }

    private fun getAlmostAppropriateConstructions(constructionsId: ArrayList<Int>) {
        getConstructionsToDisplay(
            constructionsId, almostAppropriateConstructions,
            "http://192.168.0.104:9595/api/almost_appropriate_construction/?",
            this::showAlmostAppropriateConstructions
        )
    }

    private fun showConstructionsToDisplay(
        constructionsContainer: ArrayList<Triple<Int, String, String>>,
        resourceId: Int
    ) {
        val imageList = java.util.ArrayList<SlideModel>()
        for (construction in constructionsContainer) {
            val constructionName: String = construction.second
            val constructionImage: String = construction.third
            imageList.add(SlideModel(constructionImage, constructionName, ScaleTypes.FIT))
        }
        val imageSlider = findViewById<ImageSlider>(resourceId)
        imageSlider.setImageList(imageList)
    }

    private fun showAppropriateConstructions() {
        textView35.visibility = ViewGroup.VISIBLE
        appropriate_slider.visibility = ViewGroup.VISIBLE
        showConstructionsToDisplay(appropriateConstructions,
            R.id.appropriate_slider
        )
    }

    private fun showAlmostAppropriateConstructions() {
        textView38.visibility = ViewGroup.VISIBLE
        almost_appropriate_slider.visibility = ViewGroup.VISIBLE
        showConstructionsToDisplay(almostAppropriateConstructions,
            R.id.almost_appropriate_slider
        )
    }

    fun emptyCanvas(view: View) {
        buttonContainer.removeAllViews()
    }
}

class MyAdapter<T>(context: Context, layout: Int, var resource: ArrayList<T>) :
    ArrayAdapter<T>(context, layout, resource) {

    val resourceArray = resource as ArrayList<Pair<String, Int>>

    override fun getItemId(position: Int): Long {
        return resourceArray[position].second.toLong()
    }

    override fun getItem(position: Int): T? {
        return resourceArray[position].first as T?
    }
}