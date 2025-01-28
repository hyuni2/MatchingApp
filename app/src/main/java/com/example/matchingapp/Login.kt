package com.example.matchingapp

import DBManager
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import org.w3c.dom.Text


class Login : AppCompatActivity() {
    lateinit var id : EditText
    lateinit var pw : EditText
    lateinit var checkidpw : Button
    private lateinit var dbManager: DBManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.login)


        dbManager = DBManager(this, "database_name", null, 1)

        id = findViewById(R.id.id)
        pw = findViewById(R.id.pw)
        checkidpw = findViewById(R.id.checkidpw)


        checkidpw.setOnClickListener{
            val userid = id.text.toString().trim()
            //입력값 받아 문자열로 변환 뒤 공백제거
            val userpw = pw.text.toString().trim()

            if (dbManager.loginUser(userid, userpw)){
                //Id, pw db 정보와 일치할 시 MYpage로 이동 .
                val fragment = FindMentoMenti_fragment() // 프래그먼트 인스턴스 생성
                val transaction = supportFragmentManager.beginTransaction()
                transaction.replace(R.id.fragmentContainer, fragment) // R.id.fragment_container는 프래그먼트를 담을 컨테이너의 ID입니다.
                transaction.addToBackStack(null) // 백스택에 추가하여 뒤로 가기 버튼을 눌렀을 때 이전 화면으로 돌아갈 수 있게 합니다.
                transaction.commit()
            } else {
                //Id, pw db 정보와 불일치할 시 오류메시지 출력. (개발조건)
                Toast.makeText(this, "잘못된 정보입니다.", Toast.LENGTH_SHORT).show()
            }

        }




        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}