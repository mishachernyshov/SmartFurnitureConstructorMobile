package com.example.smartfurnitureconstructor.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.smartfurnitureconstructor.R

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class Shop : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
                          savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_shop, container, false)
    }

}