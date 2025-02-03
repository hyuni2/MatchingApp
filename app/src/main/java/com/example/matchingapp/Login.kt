package com.example.matchingapp

import com.example.matchingapp.DBManager
import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.content.SharedPreferences
import org.w3c.dom.Text


class Login : AppCompatActivity() {
    lateinit var id : EditText
    lateinit var pw : EditText
    lateinit var checkidpw : ImageButton
    lateinit var backbtlogin : ImageButton
    private lateinit var dbManager: DBManager
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)


        dbManager = DBManager(this, "MatchingAppDB", null, 1)
        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE) //로그인시 id 프로필 연동하기 위해 추가

        id = findViewById(R.id.id)
        pw = findViewById(R.id.pw)
        checkidpw = findViewById(R.id.checkidpw)
        backbtlogin = findViewById(R.id.backbtlogin)


        backbtlogin.setOnClickListener {
            val intent = Intent(this, FirstPageToLoginAndRegister::class.java)
            startActivity(intent)
        }

        checkidpw.setOnClickListener{
            val userid = id.text.toString().trim()
            //입력값 받아 문자열로 변환 뒤 공백제거
            val userpw = pw.text.toString().trim()

            if (dbManager.loginUser(userid, userpw)){
                // 로그인 성공 시 아이디 저장
                val editor = sharedPreferences.edit()
                editor.putString("loggedInUser", userid)
                editor.apply()

                // 로그인 후 MainActivity로 이동
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()  // 로그인 화면 종료
            } else {
                //Id, pw db 정보와 불일치할 시 오류메시지 출력. (개발조건)
                Toast.makeText(this, "잘못된 정보입니다.", Toast.LENGTH_SHORT).show()
            }

        }



        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.login)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}