package com.example.matchingapp

import com.example.matchingapp.DBManager
import android.content.Intent
import android.os.Bundle
import android.os.Looper
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.w3c.dom.Text
import java.util.logging.Handler


class Register : AppCompatActivity() {
    lateinit var id : EditText
    lateinit var pw : EditText
    lateinit var Register : Button
    private lateinit var dbManager: DBManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)


        dbManager = DBManager(this, "regitsterUser", null, 1)

        id = findViewById(R.id.newid)
        pw = findViewById(R.id.newpw)
        Register = findViewById(R.id.Register)


        Register.setOnClickListener{
            val newid = id.text.toString().trim()
            //입력값 받아 문자열로 변환 뒤 공백제거
            val newpw = pw.text.toString().trim()

           var result =  dbManager.registerUser(newid, newpw)

            if (result == -1L)
            //아이디가 이미 존재할 경우 오류 메시지 출력
            {
                Toast.makeText(this, "이미 존재하는 아이디입니다", Toast.LENGTH_SHORT).show()

            } else {
                //아니면 회원가입 성공.
                Toast.makeText(this, "회원가입이 완료되었습니다. 로그인 페이지로 이동합니다.", Toast.LENGTH_SHORT).show()
                lifecycleScope.launch {
                    delay(2000) // 2초 대기
                    val intent = Intent(this@Register, Login::class.java)
                    startActivity(intent)
                    finish()
                }



            }

        }




        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.Register)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}