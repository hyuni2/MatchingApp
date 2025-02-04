package com.example.matchingapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.widget.ImageButton
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import android.widget.CheckBox
import android.text.Editable
import android.text.TextWatcher
import com.example.matchingapp.DBManager

class ProfileEditActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_edit)

        // UI 요소 연결
        val backbt1: ImageButton = findViewById(R.id.backbt1)
        val editName: EditText = findViewById(R.id.editName)
        val editMajor: EditText = findViewById(R.id.editMajor)
        val profileintro: EditText = findViewById(R.id.Profileintro)  // 소개글 입력란
        val profileEditDoneButton: Button = findViewById(R.id.ProfileEditDoneButton)
        val mentorCheckBox: CheckBox = findViewById(R.id.MentorCheckBox)  // 멘토 체크박스
        val menteeCheckBox: CheckBox = findViewById(R.id.MenteeCheckBox)  // 멘티 체크박스

        // 현재 로그인한 ID (SharedPreferences)
        val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val currentUserId = sharedPreferences.getString("loggedInUser", "정보 없음") ?: "정보 없음"

        // DBManager 초기화
        val dbManager = DBManager(this, "MatchingAppDB", null, 1)

        // 기존 프로필 정보 불러오기
        if (currentUserId != "정보 없음") {
            val profile = dbManager.getProfile(currentUserId)  // DB에서 프로필 정보 가져오기
            profile?.let {
                // 기존 정보가 있을 경우, EditText에 세팅
                editName.setText(it.name)
                editMajor.setText(it.major)
                profileintro.setText(it.intro)

                // 멘토/멘티 체크박스 설정
                when (it.isMentor) {
                    1 -> {
                        mentorCheckBox.isChecked = true
                        menteeCheckBox.isChecked = false
                    }
                    0 -> {
                        mentorCheckBox.isChecked = false
                        menteeCheckBox.isChecked = true
                    }
                    else -> {
                        mentorCheckBox.isChecked = false
                        menteeCheckBox.isChecked = false
                    }
                }
            }
        }

        // 버튼 비활성화 초기화
        profileEditDoneButton.isEnabled = false

        // 입력 필드와 체크박스 상태 변경 시 버튼 활성화 체크
        val checkFields = {
            profileEditDoneButton.isEnabled =
                editName.text.isNotEmpty() && editMajor.text.isNotEmpty() && profileintro.text.isNotEmpty() &&
                        (mentorCheckBox.isChecked || menteeCheckBox.isChecked)
        }

        // 텍스트 입력 감지
        val textWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                checkFields()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }

        editName.addTextChangedListener(textWatcher)
        editMajor.addTextChangedListener(textWatcher)
        profileintro.addTextChangedListener(textWatcher)

        // 체크박스 변경 감지
        mentorCheckBox.setOnCheckedChangeListener { _, _ -> checkFields() }
        menteeCheckBox.setOnCheckedChangeListener { _, _ -> checkFields() }

        profileEditDoneButton.setOnClickListener {
            val newName = editName.text.toString()
            val newMajor = editMajor.text.toString()
            val newisMentor = when {
                mentorCheckBox.isChecked -> 1
                menteeCheckBox.isChecked -> 0
                else -> -1
            }
            val newIntro = profileintro.text.toString()  // 소개글



            // 프로필 업데이트
            val updateSuccess = dbManager.updateProfile(currentUserId, newName, newisMentor, newMajor, newIntro)

            if (updateSuccess) {
                Toast.makeText(this, "프로필이 성공적으로 업데이트되었습니다.", Toast.LENGTH_SHORT).show()
                val resultIntent = Intent()
                resultIntent.putExtra("isProfileUpdated", true)
                setResult(RESULT_OK, resultIntent)
                finish()
            } else {
                Toast.makeText(this, "프로필 업데이트에 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
        }
        // 체크박스 중복 선택 방지
        mentorCheckBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                menteeCheckBox.isChecked = false  // 멘티 체크박스 해제
            }
        }

        menteeCheckBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                mentorCheckBox.isChecked = false  // 멘토 체크박스 해제
            }
        }

        backbt1.setOnClickListener {
            // 현재 액티비티 종료 (이전 화면으로 돌아감)
            finish()
        }
    }
}
