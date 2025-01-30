package com.example.matchingapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

class ProfileEditActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_edit)

        // UI 요소 연결
        val editName: EditText = findViewById(R.id.editName)
        val editMajor: EditText = findViewById(R.id.editMajor)
        val profileEditDoneButton: Button = findViewById(R.id.ProfileEditDoneButton)
        val userDeleteButton: Button = findViewById(R.id.UserDeleteButton)

        // 현재 로그인한 ID (SharedPreferences)
        val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val currentUserId = sharedPreferences.getString("loggedInUser", "정보 없음") ?: "정보 없음"

        // DBManager 초기화
        val dbManager = DBManager(this, "MatchingAppDB", null, 1)

        profileEditDoneButton.setOnClickListener {
            // 사용자 입력 값 가져오기
            val newName = editName.text.toString()
            val newMajor = editMajor.text.toString()

            // 닉네임과 전공이 비어있지 않은지 확인
            if (newName.isNotEmpty() && newMajor.isNotEmpty()) {
                // 프로필 업데이트
                val updateSuccess = dbManager.updateProfile(currentUserId, newName, newMajor)

                if (updateSuccess) {
                    // 저장이 성공하면 토스트 메시지 출력
                    Toast.makeText(this, "프로필이 성공적으로 업데이트되었습니다.", Toast.LENGTH_SHORT).show()
                    // 액티비티 종료 후 이전 화면으로 돌아가기
                    finish()
                } else {
                    // 닉네임이 중복된 경우
                    Toast.makeText(this, "이미 사용 중인 닉네임입니다. 다른 닉네임을 입력하세요.", Toast.LENGTH_SHORT).show()
                }
            } else {
                // 닉네임이나 전공이 비어있으면 오류 메시지 출력
                Toast.makeText(this, "닉네임과 전공을 모두 입력해주세요.", Toast.LENGTH_SHORT).show()
            }
        }

        // 회원 탈퇴 버튼 클릭 리스너
        userDeleteButton.setOnClickListener {
            // DB에서 사용자 정보 삭제
            val deleteSuccess = dbManager.deleteUser(currentUserId)

            if (deleteSuccess) {
                // 탈퇴 성공하면 토스트 메시지 출력
                Toast.makeText(this, "회원 탈퇴가 완료되었습니다.", Toast.LENGTH_SHORT).show()

                // SharedPreferences에서 로그인된 사용자 정보 삭제
                val editor = sharedPreferences.edit()
                editor.remove("loggedInUser")
                editor.apply()

                // 시작 화면으로 돌아가기 (MainActivity로 이동)
                val intent = Intent(this, FirstPageToLoginAndRegister::class.java)
                startActivity(intent)
                finish() // 현재 액티비티 종료
            } else {
                // 탈퇴 실패 메시지
                Toast.makeText(this, "회원 탈퇴에 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
        }

    }
}