package com.example.matchingapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import android.content.Context

class ProfileDetailFragment : Fragment() {
    private lateinit var dbManager: DBManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dbManager = DBManager(requireContext(), "MatchingAppDB", null, 1)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile_detail, container, false)

        // 전달받은 데이터 처리
        val name = arguments?.getString("name")
        val role = arguments?.getString("role")
        val major = arguments?.getString("major")
        val intro = arguments?.getString("intro")

        // UI 연결 및 데이터 표시
        view.findViewById<TextView>(R.id.nameText).text = name
        view.findViewById<TextView>(R.id.roleText).text = role
        view.findViewById<TextView>(R.id.majorText).text = major
        view.findViewById<TextView>(R.id.introText).text = intro

        val applyButton = view.findViewById<Button>(R.id.applyButton)

        // 현재 사용자 ID 및 상대방 ID 가져오기
        val senderId = getCurrentUserId()
        val receiverId = name?.let { getUserIdByName(it) }

        if (receiverId != null) {
            when {
                senderId == receiverId -> {
                    applyButton.isEnabled = false
                    applyButton.text = "자기 자신에게 요청 불가"
                }
                requestSent(senderId, receiverId) -> {
                    applyButton.isEnabled = false
                    applyButton.text = "요청 완료"
                }
                else -> {
                    applyButton.isEnabled = true
                    applyButton.text = "매칭 요청"
                }
            }

            applyButton.setOnClickListener {
                sendMatchRequest(receiverId, applyButton)
            }
        } else {
            applyButton.isEnabled = false
            applyButton.text = "요청 불가"
        }

        return view
    }

    // 🔹 receiverId를 직접 매개변수로 받도록 수정
    private fun sendMatchRequest(receiverId: String, button: Button) {
        if (receiverId.isEmpty()) {
            Toast.makeText(requireContext(), "잘못된 프로필 정보입니다.", Toast.LENGTH_SHORT).show()
            return
        }

        val senderId = getCurrentUserId()

        if (senderId == receiverId) {
            Toast.makeText(requireContext(), "자기 자신에게는 매치 요청을 보낼 수 없습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        val success = dbManager.insertMatchRequest(senderId, receiverId)
        if (success) {
            Toast.makeText(requireContext(), "매치 요청이 전달되었습니다.", Toast.LENGTH_SHORT).show()
            button.apply {
                isEnabled = false
                text = "요청 완료"
            }
        } else {
            Toast.makeText(requireContext(), "매치 요청에 실패했습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    // 현재 로그인한 사용자 ID 가져오는 함수
    private fun getCurrentUserId(): String {
        val sharedPreferences = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString("loggedInUser", "") ?: ""
    }

    // 요청받는 사용자의 ID 가져오는 함수
    private fun getUserIdByName(name: String): String? {
        val db = dbManager.readableDatabase
        val cursor = db.rawQuery("SELECT userid FROM Profile WHERE name=?", arrayOf(name))
        var userId: String? = null
        if (cursor.moveToFirst()) {
            userId = cursor.getString(0)
        }
        cursor.close()
        return userId
    }

    // 요청을 보냈는지 확인하는 함수
    private fun requestSent(senderId: String, receiverId: String): Boolean {
        val db = dbManager.readableDatabase
        val cursor = db.rawQuery(
            "SELECT COUNT(*) FROM MatchRequest WHERE sender = ? AND receiver = ?",
            arrayOf(senderId, receiverId)
        )

        var requestExists = false
        if (cursor.moveToFirst()) {
            requestExists = cursor.getInt(0) > 0
        }
        cursor.close()
        return requestExists
    }
}
