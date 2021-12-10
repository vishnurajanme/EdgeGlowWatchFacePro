package com.vishnu.edgeglowwatchfacepro

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.support.wearable.activity.WearableActivity
import com.google.android.material.slider.Slider


class ConfigurationActivity : WearableActivity() {

    lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_config)
        // Enables Always-on
        setAmbientEnabled()
        val dateswitch = findViewById<com.google.android.material.switchmaterial.SwitchMaterial>(R.id.Date)
        val edgeglowswitch = findViewById<com.google.android.material.switchmaterial.SwitchMaterial>(R.id.edgeglow)
        val digitglowswitch = findViewById<com.google.android.material.switchmaterial.SwitchMaterial>(R.id.digitglow)

        val sliderTime = findViewById<Slider>(R.id.sliderDigit)
        val sliderDate = findViewById<Slider>(R.id.sliderDate)
        val sliderSpacing = findViewById<Slider>(R.id.sliderSpacing)

        sharedPreferences = getSharedPreferences("edgeglowwatchface", Context.MODE_PRIVATE)

        val timeSize = sharedPreferences.getFloat("timeSize", 200f)
        val dateSize = sharedPreferences.getFloat("dateSize", 10f)
        val spacingSize = sharedPreferences.getFloat("spacingSize", 20f)

        sliderTime.value = timeSize
        sliderDate.value = dateSize
        sliderSpacing.value = spacingSize

        val dateflag = sharedPreferences.getInt("dateflag", 1)
        val edgeglowflag = sharedPreferences.getInt("edgeglowflag", 1)
        val digitglowflag = sharedPreferences.getInt("digitglowflag", 1)

        if (dateflag == 1) {
            dateswitch.toggle()
        }

        if (edgeglowflag == 1) {
            edgeglowswitch.toggle()
        }

        if (digitglowflag == 1) {
            digitglowswitch.toggle()
        }


        dateswitch.setOnClickListener {
            if (dateswitch.isChecked) {
                sharedPreferences.edit().putInt("dateflag", 1).apply()
            } else {
                sharedPreferences.edit().putInt("dateflag", 0).apply()
            }
        }

        edgeglowswitch.setOnClickListener {
            if (edgeglowswitch.isChecked) {
                sharedPreferences.edit().putInt("edgeglowflag", 1).apply()
            } else {
                sharedPreferences.edit().putInt("edgeglowflag", 0).apply()
            }
        }

        digitglowswitch.setOnClickListener {
            if (digitglowswitch.isChecked) {
                sharedPreferences.edit().putInt("digitglowflag", 1).apply()
            } else {
                sharedPreferences.edit().putInt("digitglowflag", 0).apply()
            }
        }

        sliderTime.addOnChangeListener { slider, value, fromUser ->

            sharedPreferences.edit().putFloat("timeSize", value).apply()

            // Responds to when slider's value is changed
        }


        sliderDate.addOnChangeListener { slider, value, fromUser ->

            sharedPreferences.edit().putFloat("dateSize", value).apply()

            // Responds to when slider's value is changed
        }

        sliderSpacing.addOnChangeListener { slider, value, fromUser ->

            sharedPreferences.edit().putFloat("spacingSize", value).apply()

            // Responds to when slider's value is changed
        }


    }


}
