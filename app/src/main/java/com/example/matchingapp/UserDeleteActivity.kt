package com.example.matchingapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.matchingapp.DBManager

class UserDeleteActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_delete)

        val userDeleteButton: Button = findViewById(R.id.UserDeleteButton)

        userDeleteButton.setOnClickListener {
            val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
            val currentUserId = sharedPreferences.getString("loggedInUser", null)

            if (currentUserId != null) {
                val dbManager = DBManager(this, "MatchingAppDB", null, 1)
                val deleteSuccess = dbManager.deleteUser(currentUserId)

                if (deleteSuccess) {
                    Toast.makeText(this, "회원 탈퇴가 완료되었습니다.", Toast.LENGTH_SHORT).show()

                    // 로그인 정보 삭제
                    sharedPreferences.edit().remove("loggedInUser").apply()

                    // 로그인 화면으로 이동
                    val intent = Intent(this, FirstPageToLoginAndRegister::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "회원 탈퇴에 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "로그인된 사용자 정보가 없습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}