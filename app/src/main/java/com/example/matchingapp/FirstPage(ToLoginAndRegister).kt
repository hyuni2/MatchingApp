package com.example.matchingapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class `FirstPage(ToLoginAndRegister)` : AppCompatActivity() {
    lateinit var login : Button
    lateinit var join : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.first)

        login = findViewById<Button>(R.id.login)
        join = findViewById<Button>(R.id.join)

        login.setOnClickListener{
            var intent = Intent(this, Login::class.java)
            startActivity(intent)

        }

        join.setOnClickListener{
            var intent = Intent(this, Login::class.java)
            startActivity(intent)
        }


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}