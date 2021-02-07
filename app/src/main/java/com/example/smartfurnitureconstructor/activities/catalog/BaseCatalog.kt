package com.example.smartfurnitureconstructor.activities.catalog

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SearchView
import com.android.volley.VolleyError
import com.android.volley.toolbox.Volley
import com.example.smartfurnitureconstructor.R
import com.example.smartfurnitureconstructor.activities.base.ActivityWithTopMenu
import com.example.smartfurnitureconstructor.dataclasses.CatalogFitable
import kotlinx.android.synthetic.main.activity_component_catalog.*

open class BaseCatalog : ActivityWithTopMenu() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_component_catalog)

        requestQueue = Volley.newRequestQueue(this)

        fillSortingSpinner()
        setSearchRequestListener()
    }

    private fun fillSortingSpinner() {
        val arrayList: ArrayList<String> = ArrayList()
        arrayList.add(resources.getString(R.string.catalog_sorting_order_by_name))
        arrayList.add(resources.getString(R.string.catalog_sorting_ascending_rating_order))
        arrayList.add(resources.getString(R.string.catalog_sorting_descending_rating_order))
        val arrayAdapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, arrayList)
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = arrayAdapter
    }

    fun setSortingSpinnerItemsListener(entities: ArrayList<CatalogFitable>) {
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parentView: AdapterView<*>?,
                selectedItemView: View,
                position: Int,
                id: Long
            ) {
                when(position) {
                    0 -> entities.sortWith(nameComparator)
                    1 -> entities.sortWith(rateAscendingComparator)
                    2 -> entities.sortWith(rateDescendingComparator)
                }
                fillContainer()
            }

            override fun onNothingSelected(parentView: AdapterView<*>?) {
            }
        }
    }

    private fun setSearchRequestListener() {
        searchView.setOnQueryTextListener (object : SearchView.OnQueryTextListener {
            override fun onQueryTextChange(newText: String): Boolean {
                fillContainer()
                return false
            }

            override fun onQueryTextSubmit(query: String): Boolean {
                fillContainer()
                return false
            }
        })
    }

    val nameComparator = Comparator<CatalogFitable> { object1, object2 ->
        val firstName = object1.name
        val secondName = object2.name
        when {
            firstName > secondName -> 1
            firstName < secondName -> -1
            else -> 0
        }
    }

    val rateAscendingComparator = Comparator<CatalogFitable> { object1, object2 ->
        object1.rating - object2.rating
    }

    val rateDescendingComparator = Comparator<CatalogFitable> { object1, object2 ->
        object2.rating - object1.rating
    }

    open fun fillContainer() {
    }

    fun getAllEntities(url: String) {
        sendJsonArrayGetRequestWithoutHeader(url, requestQueue,
            this::executeIfGettingEntitiesIsSuccessful, arrayListOf(),
            this::executeIfGettingEntitiesIsFailed, arrayListOf())
    }

    private fun executeIfGettingEntitiesIsSuccessful(
        response: Any,
        args: ArrayList<Any?>) {
        fillingEntitiesArray(response)
    }

    private fun executeIfGettingEntitiesIsFailed(
        error: VolleyError,
        args: ArrayList<Any?>) {}

    open fun fillingEntitiesArray(response: Any) {
    }
}