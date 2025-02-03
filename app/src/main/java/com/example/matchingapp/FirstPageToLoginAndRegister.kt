package com.example.matchingapp

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class FirstPageToLoginAndRegister : AppCompatActivity() {
    lateinit var login : ImageButton
    lateinit var join : ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.firstpage)

        login = findViewById<ImageButton>(R.id.login)
        join = findViewById<ImageButton>(R.id.join)

        login.setOnClickListener{
            var intent = Intent(this, Login::class.java)
            startActivity(intent)
            finish()

        }

        join.setOnClickListener{
            var intent = Intent(this, Register::class.java)
            startActivity(intent)
            finish()
        }


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}