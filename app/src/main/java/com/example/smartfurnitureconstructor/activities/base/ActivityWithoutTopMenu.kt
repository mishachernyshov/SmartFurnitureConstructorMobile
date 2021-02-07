package com.example.smartfurnitureconstructor.activities.base

import android.os.Bundle
import android.view.MenuItem
import com.example.smartfurnitureconstructor.R

open class ActivityWithoutTopMenu : GeneralApplicationActivity() {
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        changeUserInterfaceLanguage()
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityMenu = R.menu.change_language_menu
    }
}