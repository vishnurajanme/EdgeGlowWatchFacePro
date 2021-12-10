package com.vishnu.edgeglowwatchfacepro

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.net.Uri


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val talk = findViewById<com.google.android.material.button.MaterialButton>(R.id.talk)
        val feedback = findViewById<com.google.android.material.button.MaterialButton>(R.id.feedback)

        val talkintent = Intent(Intent.ACTION_VIEW, Uri.parse("https://drvishnurajan.wordpress.com"))
        val feedbackintent = Intent(Intent.ACTION_VIEW, Uri.parse("https://drvishnurajan.wordpress.com/report-a-bug-suggestion"))

        talk.setOnClickListener {
            startActivity(talkintent);
        }

        feedback.setOnClickListener {
            startActivity(feedbackintent);
        }


    }
}