package com.example.smartfurnitureconstructor.activities.catalog

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.DialogFragment
import com.example.smartfurnitureconstructor.R
import com.example.smartfurnitureconstructor.dataclasses.CatalogFitable
import com.example.smartfurnitureconstructor.dataclasses.Component
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_component_catalog.*
import kotlinx.android.synthetic.main.fragment_alert_dialog_canvas.view.*
import kotlinx.android.synthetic.main.fragment_catalog_component.view.*
import kotlinx.android.synthetic.main.fragment_unfilding_rating_interval.view.*
import kotlinx.android.synthetic.main.fragment_unfolding_item.view.*
import org.json.JSONArray
import java.net.URL


class ComponentCatalog : BaseCatalog() {
    val components: ArrayList<Component> = arrayListOf()
    val categories: MutableSet<String> = mutableSetOf()
    val manufacturers: MutableSet<String> = mutableSetOf()
    val chosenNames: ArrayList<String> = arrayListOf()
    val chosenCategories: MutableSet<String> = mutableSetOf()
    val chosenManufacturers: MutableSet<String> = mutableSetOf()
    private val filterAlert: FilterComponentsDialogFragment =
        FilterComponentsDialogFragment(
            this
        )
    val componentsUrl: String = "http://192.168.0.104:9595/api/component/"
    var minRate: Float = 1.0F
    var maxRate: Float = 5F

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setSortingSpinnerItemsListener(components as ArrayList<CatalogFitable>)
        getAllEntities(componentsUrl)
    }

    override fun fillingEntitiesArray(
        response: Any
    ) {
        val responseArray = response as JSONArray
        for (i in 0 until responseArray.length()) {
            val currentObject = responseArray.getJSONObject(i)
            components.add(
                Component(
                    currentObject.getInt("id"),
                    currentObject.getString("name"),
                    currentObject.getString("image"),
                    currentObject.getString("description"),
                    currentObject.getInt("rating"),
                    currentObject.getString("category"),
                    currentObject.getString("manufacturer")
                )
            )
            categories.add(currentObject.getString("category"))
            manufacturers.add(currentObject.getString("manufacturer"))
        }
        components.sortWith(nameComparator)
        fillContainer()
    }

    fun showFilterDialog(view: View) {
        filterAlert.show(supportFragmentManager, "filtration")
    }

    override fun fillContainer() {
        components_container.removeAllViews()
        val displayedComponents: LinearLayout =
            components_container
        val inflater: LayoutInflater =
            LayoutInflater.from(this)
        var componentCategory: String
        var componentManufacturer: String
        var componentRating: Float
        val componentCategoryCount = chosenCategories.size
        val componentManufacturerCount =
            chosenManufacturers.size
        val componentSearchCriteria =
            searchView.query.toString().toLowerCase()
        val searchCriteriaLength =
            componentSearchCriteria.length
        var componentNameStandardView: String
        chosenNames.clear()
        for (component in components) {
            componentNameStandardView =
                component.name.toLowerCase()
            componentCategory = component.category
            componentManufacturer = component.manufacturer
            componentRating = component.rating.toFloat() / 2
            if ((componentCategoryCount == 0 ||
                        componentCategory in chosenCategories) &&
                (componentManufacturerCount == 0 ||
                        componentManufacturer in chosenManufacturers) &&
                (componentRating in minRate..maxRate) &&
                (componentNameStandardView.indexOf(
                    componentSearchCriteria) != -1 ||
                        searchCriteriaLength == 0)) {
                chosenNames.add(component.name)
                val view = inflater.inflate(
                    R.layout.fragment_catalog_component,
                    displayedComponents, false)
                view.textView14.text = component.name
                view.textView14.tag = component.id
                view.textView15.text = component.description
                view.textView31.text = componentCategory
                view.textView32.text = componentManufacturer
                val url: URL = URL(component.image)
                Picasso.get().load(url.toString()).into(view.imageView3)
                view.imageView3.tag = component.id
                view.ratingBar2.rating = componentRating
                displayedComponents.addView(view)
            }
        }
    }

    fun openSingleComponent(view: View) {
        val componentIntent = Intent(this,
            SingleComponent::class.java)
        componentIntent.putExtra("componentId", view.tag as Int)
        startActivity(componentIntent)
    }
}

class FilterComponentsDialogFragment(catalog: ComponentCatalog) : DialogFragment() {
    private val categories: MutableSet<String> = catalog.categories
    private val manufacturers: MutableSet<String> = catalog.manufacturers
    private val chosenCategories: MutableSet<String> = catalog.chosenCategories
    private val chosenManufacturers: MutableSet<String> = catalog.chosenManufacturers
    private val catalogReference = catalog

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)

            chosenCategories.clear()

            val inflater = requireActivity().layoutInflater
            val canvasView = inflater.inflate(R.layout.fragment_alert_dialog_canvas, null)
            val unfoldingItems: LinearLayout = canvasView.filterItem
            val unfoldingItemInflater: LayoutInflater = LayoutInflater.from(context)
            var expandableButton: Button
            var expandableCharacteristicsView: ConstraintLayout
            var expandableCard: CardView

            val categoryView = unfoldingItemInflater.inflate(
                R.layout.fragment_unfolding_item, unfoldingItems, false)
            addCheckBoxesToView(categoryView, categories, chosenCategories)
            categoryView.textView111.text = resources.getString(R.string.catalog_filter_category)
            unfoldingItems.addView(categoryView)

            expandableButton = categoryView.unfolding_item_characteristics_arrow_button
            expandableCharacteristicsView = categoryView.unfolding_item_expandable_constraint
            expandableCard = categoryView.expandable_card
            setListeners(expandableButton, expandableCharacteristicsView, expandableCard)

            val manufacturerView = unfoldingItemInflater.inflate(
                R.layout.fragment_unfolding_item, unfoldingItems, false)
            manufacturerView.textView111.text = resources.getString(R.string.catalog_filter_manufacturer)
            addCheckBoxesToView(manufacturerView, manufacturers, chosenManufacturers)
            unfoldingItems.addView(manufacturerView)
            expandableButton = manufacturerView.unfolding_item_characteristics_arrow_button
            expandableCharacteristicsView = manufacturerView.unfolding_item_expandable_constraint
            expandableCard = manufacturerView.expandable_card
            setListeners(expandableButton, expandableCharacteristicsView, expandableCard)

            val ratingView = unfoldingItemInflater.inflate(
                R.layout.fragment_unfilding_rating_interval,
                unfoldingItems, false)
            ratingView.textView1.text = resources.getString(R.string.catalog_rating)
            ratingView.editTextNumberSigned5.setText(catalogReference.minRate.toString())
            ratingView.editTextNumberSigned4.setText(catalogReference.maxRate.toString())
            unfoldingItems.addView(ratingView)
            expandableButton = ratingView.unfolding_rating_arrow_button
            expandableCharacteristicsView = ratingView.unfolding_rating_expandable_constraint
            expandableCard = ratingView.expandable_rating_card
            setListeners(expandableButton, expandableCharacteristicsView, expandableCard)

            builder.setTitle(resources.getString(R.string.catalog_filtration))
                .setView(canvasView)
                .setPositiveButton(resources.getString(R.string.confirm),
                    DialogInterface.OnClickListener { _, _ ->
                        catalogReference.minRate = ratingView.editTextNumberSigned5
                            .text.toString().toFloat()
                        catalogReference.maxRate = ratingView.editTextNumberSigned4
                            .text.toString().toFloat()
                        catalogReference.fillContainer()
                    })
            val alert = builder.create()
            alert.setOnShowListener {
                val btnPositive = alert.getButton(Dialog.BUTTON_POSITIVE)
                btnPositive.textSize = 17F
                val btnNegative = alert.getButton(Dialog.BUTTON_NEGATIVE)
                btnNegative.textSize = 17F
            }

            return alert
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    private fun addCheckBoxesToView(view: View,
                                    itemSet: MutableSet<String>,
                                    filterParamsSet: MutableSet<String>) {
        for (i in itemSet) {
            val currentCheckBox: CheckBox = CheckBox(context)
            currentCheckBox.text = i
            currentCheckBox.textSize = 17F
            currentCheckBox.setOnClickListener {
                if (currentCheckBox.isChecked) {
                    filterParamsSet.add(currentCheckBox.text.toString())
                } else {
                    filterParamsSet.remove(currentCheckBox.text.toString())
                }
            }
            view.unfoldingItemListLayout.addView(currentCheckBox)
        }
    }

    private fun setListeners(button: Button, expandedView: ConstraintLayout, card: CardView) {
        button.setOnClickListener {
            if (expandedView.visibility == View.GONE) {
                TransitionManager.beginDelayedTransition(
                    card, AutoTransition()
                )
                expandedView.visibility = View.VISIBLE
                button.setBackgroundResource(R.drawable.ic_baseline_keyboard_arrow_up_24)
            } else {
                TransitionManager.beginDelayedTransition(
                    card, AutoTransition()
                )
                expandedView.visibility = View.GONE
                button.setBackgroundResource(R.drawable.ic_baseline_keyboard_arrow_down_24)
            }
        }
    }
}