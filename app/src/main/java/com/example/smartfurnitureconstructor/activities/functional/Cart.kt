package com.example.smartfurnitureconstructor.activities.functional

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TableLayout
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.doAfterTextChanged
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.smartfurnitureconstructor.R
import com.example.smartfurnitureconstructor.activities.base.ActivityWithTopMenu
import kotlinx.android.synthetic.main.activity_cart.*
import kotlinx.android.synthetic.main.fragment_cart_row.view.*
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.roundToInt
import kotlin.properties.Delegates

class Cart : ActivityWithTopMenu() {
    private var exchangeRates by Delegates.notNull<Float>()
    private var convertedPrice by Delegates.notNull<Float>()
    private var totalSum: Float = 0F
    private val idsToInitialCountCorresponding: MutableMap<Int, Int> = mutableMapOf()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)
        getExchangeRates()
    }

    private fun getCartContent() {
        val url = "http://192.168.0.104:9595/api/cart_content/"

        val queue = Volley.newRequestQueue(this)
        val req : JsonObjectRequest =
            object : JsonObjectRequest(Request.Method.GET, url, null,
            Response.Listener { response ->
                val cartTable: TableLayout = tableLayout
                val inflater: LayoutInflater = LayoutInflater.from(this)
                var currentGood: JSONArray
                var goodNumber: Int
                var currentSum: Float = 0F
                var goodId: Int
                for (k in response.keys()) {
                    currentGood = response.getJSONArray(k)
                    val view = inflater.inflate(
                        R.layout.fragment_cart_row,
                        cartTable, false)
                    goodNumber = currentGood.getInt(0)
                    goodId = currentGood.getInt(1)
                    view.textView39.text = k
                    view.editTextNumber.setText(goodNumber.toString())
                    view.textView40.text = convertedPrice.toString() + " " +
                            resources.getString(R.string.cart_currency)
                    currentSum = getRoundedFloatingCost(convertedPrice * goodNumber)
                    totalSum += currentSum
                    view.textView41.text = currentSum.toString() + " " +
                            resources.getString(R.string.cart_currency)
                    view.tag = goodId
                    changeGoodNumberHandler(view)
                    deleteGoodFromCartHandler(view)
                    idsToInitialCountCorresponding[goodId] = goodNumber
                    cartTable.addView(view)
                }
                totalSum = getRoundedFloatingCost(totalSum)
                textView47.text = totalSum.toString() + " " +
                        resources.getString(R.string.cart_currency)
            }, Response.ErrorListener {
            }) {
            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                return getJwtHeaderParams()
            }
        }
        queue.add(req)
    }

    private fun getExchangeRates() {
        val url = "https://api.privatbank.ua/p24api/pubinfo?json&exchange&coursid=5"

        val queue = Volley.newRequestQueue(this)
        val req = JsonArrayRequest(Request.Method.GET, url, null,
            Response.Listener { response ->
                exchangeRates = getRoundedFloatingCost(response.getJSONObject(0)
                    .getString("buy").toFloat())
                convertedPrice = if (currentLocale == "en") 1F
                    else exchangeRates
                getCartContent()
            }, Response.ErrorListener {
            })
        queue.add(req)
    }

    private fun changeGoodNumberHandler(view: View) {
        view.editTextNumber.doAfterTextChanged {
            var newGoodCountValue: String = it.toString()
            if (newGoodCountValue.isEmpty()) {
                newGoodCountValue = "0"
            }
            val goodCost = getFloatFromStringStart(view.textView41.text.toString())
            val newGoodCount: Int = newGoodCountValue.toInt()
            val newGoodCost: Float = newGoodCount * convertedPrice
            view.textView41.text = newGoodCost.toString() + " " +
                    resources.getString(R.string.cart_currency)
            val oldGoodCount = (goodCost / convertedPrice).roundToInt()
            val countDifference = newGoodCount - oldGoodCount
            val totalPriceChange = countDifference * convertedPrice
            totalSum = getRoundedFloatingCost(totalPriceChange + totalSum)
            textView47.text = totalSum.toString() + " " +
                    resources.getString(R.string.cart_currency)
        }
    }

    private fun deleteGoodFromCartHandler(view: View) {
        view.imageView5.setOnClickListener{
            val currentGoodPrice = getFloatFromStringStart(view.textView41.text.toString())
            totalSum -= currentGoodPrice
            textView47.text = totalSum.toString()
            val tableView = view.parent as ViewGroup
            tableView.removeView(view)
        }
    }

    private fun getRoundedFloatingCost(cost: Float): Float {
        val floatRepresentationWithAppropriatePrecision: Float =
            cost * 100
        val intRepresentationWithAppropriatePrecision =
            floatRepresentationWithAppropriatePrecision.roundToInt()
        return intRepresentationWithAppropriatePrecision.toFloat() / 100
    }

    private fun getFloatFromStringStart(stringValue: String): Float {
        return stringValue.replace("[^0-9.]".toRegex(), "").toFloat()
    }

    fun emptyCart(view: View) {
        tableLayout.removeViews(1, tableLayout.childCount - 1)
        totalSum = 0F
        textView47.text = totalSum.toString() + " " +
                resources.getString(R.string.cart_currency)

        val url = "http://192.168.0.104:9595/api/empty_cart/"

        val queue = Volley.newRequestQueue(this)
        val req : JsonObjectRequest = object : JsonObjectRequest(Request.Method.POST, url, null,
            Response.Listener {
            }, Response.ErrorListener {
            }) {
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

    fun changesConfirmation() {
        val confirmationDialog = AlertDialog.Builder(this)
        confirmationDialog.setMessage(resources.getString(R.string.cart_confirm_changes))
            .setCancelable(false)
            .setPositiveButton(resources.getString(R.string.catalog_item_ok)) { _, _ ->
            }
        val alertDialog = confirmationDialog.create()
        alertDialog.setTitle(resources.getString(R.string.cart_confirm_title))
        alertDialog.show()
    }

    fun commitCartContentChanging(view: View) {
        var currentChild: View
        val commitRequestObject: JSONObject = JSONObject()
        val goodsArray: JSONArray = JSONArray()
        var commitRequestSingleObject: JSONObject
        var currentTag: Int
        for (child in 1 until tableLayout.childCount) {
            currentChild = tableLayout.getChildAt(child)
            commitRequestSingleObject = JSONObject()
            currentTag = currentChild.tag as Int
            commitRequestSingleObject.put("construction_id", currentTag)
            commitRequestSingleObject.put(
                "initial_count", idsToInitialCountCorresponding[currentTag])
            commitRequestSingleObject.put(
                "requested_count", currentChild.editTextNumber.text.toString().toInt())
            goodsArray.put(commitRequestSingleObject)
        }
        commitRequestObject.put("new_values", goodsArray)

        val url = "http://192.168.0.104:9595/api/cart_content/"

        val queue = Volley.newRequestQueue(this)
        val req : JsonObjectRequest =
            object : JsonObjectRequest(Request.Method.POST, url, commitRequestObject,
            Response.Listener { _ ->
                changesConfirmation()
            }, Response.ErrorListener { _ ->
            }) {
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