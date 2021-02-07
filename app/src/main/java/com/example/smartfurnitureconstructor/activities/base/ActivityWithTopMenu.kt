package com.example.smartfurnitureconstructor.activities.base

import android.os.Bundle
import android.view.MenuItem
import com.example.smartfurnitureconstructor.R
import com.example.smartfurnitureconstructor.activities.functional.BoughtInstructions
import com.example.smartfurnitureconstructor.activities.functional.Cart
import com.example.smartfurnitureconstructor.activities.functional.FurnitureConstructor
import com.example.smartfurnitureconstructor.activities.authentication.MainActivity
import com.example.smartfurnitureconstructor.activities.catalog.ComponentCatalog
import com.example.smartfurnitureconstructor.activities.catalog.ConstructionCatalog

open class ActivityWithTopMenu : GeneralApplicationActivity() {
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.log_out_menu_item -> {
                eraseAccessToken()
                startNewActivity(MainActivity::class.java)
                return true
            }
            R.id.change_language_menu_item -> {
                changeUserInterfaceLanguage()
                return true
            }
            R.id.component_catalog_menu_item -> {
                startNewActivity(ComponentCatalog::class.java)
                return true
            }
            R.id.construction_catalog_menu_item -> {
                startNewActivity(ConstructionCatalog::class.java)
                return true
            }
            R.id.cart_menu_item -> {
                startNewActivity(Cart::class.java)
                return true
            }
            R.id.constructor_menu_item -> {
                startNewActivity(FurnitureConstructor::class.java)
                return true
            }
            R.id.instructions_menu_item -> {
                startNewActivity(BoughtInstructions::class.java)
                return true
            }
            else -> {
                return super.onOptionsItemSelected(item)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityMenu = R.menu.menu_item
    }
}