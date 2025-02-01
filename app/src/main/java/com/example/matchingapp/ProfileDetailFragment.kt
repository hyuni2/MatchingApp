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
import android.content.ContentValues
import com.example.matchingapp.DBManager
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction


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
        // 레이아웃 연걸
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

        val senderId = getCurrentUserId() // 현재 로그인한 사용자 ID 가져오기
        // name이 null이 아닌 경우에만 PDFgetUserIdByName 함수 호출
        if (name != null) {
            val receiverId: String? = dbManager.PDFgetUserIdByName(name)

            // receiverId가 null이 아닌 경우
            if (receiverId != null) {
                val applyButton = view.findViewById<Button>(R.id.applyButton)

                // 이미 매치 요청을 보낸 경우 버튼 비활성화
                if (isMatchRequestSent(senderId, receiverId)) {
                    applyButton.isEnabled = false
                } else {
                    applyButton.setOnClickListener {
                        sendMatchRequest(receiverId)  // receiverId를 매개변수로 전달
                    }
                }
            } else {
                Toast.makeText(requireContext(), "해당 사용자의 ID를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
            }
        } else {
            // name이 null인 경우 처리
            Toast.makeText(requireContext(), "프로필 이름이 잘못 전달되었습니다.", Toast.LENGTH_SHORT).show()
        }

        return view
    }

    private fun sendMatchRequest(receiverName: String?) {
        if (receiverName.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "잘못된 프로필 정보입니다.", Toast.LENGTH_SHORT).show()
            return
        }

        val senderId = getCurrentUserId() // 현재 로그인한 사용자 ID 가져오기
        val receiverId = getUserIdByName(receiverName) // 프로필의 사용자 ID 가져오기

        if (receiverId == null) {
            Toast.makeText(requireContext(), "해당 사용자의 ID를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        // 자기 자신에게 매치 요청을 보낼 수 없도록 확인
        if (senderId == receiverId) {
            Toast.makeText(requireContext(), "자기 자신에게는 매치 요청을 보낼 수 없습니다.", Toast.LENGTH_SHORT).show()

            // 버튼 비활성화
            val applyButton = view?.findViewById<Button>(R.id.applyButton)
            applyButton?.isEnabled = false

            return
        }

        val success = dbManager.insertMatchRequest(senderId, receiverId) // DBManager 사용
        if (success) {
            Toast.makeText(requireContext(), "매치 요청이 전달되었습니다.", Toast.LENGTH_SHORT).show()

            // 요청이 성공한 후 버튼 비활성화
            val applyButton = view?.findViewById<Button>(R.id.applyButton)
            applyButton?.isEnabled = false
        } else {
            Toast.makeText(requireContext(), "매치 요청에 실패했습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    // 현재 사용자 ID불러오는 함수
    private fun getCurrentUserId(): String {
        val sharedPreferences = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString("loggedInUser", "") ?: ""  // user_id 값을 가져오고, 없으면 빈 문자열 반환
    }

    // 요청받는 사람의 ID불러오는 함수
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

    // 현재 로그인한 사용자가 해당 사용자에게 이미 매치 요청을 보냈는지 확인하는 함수
    private fun isMatchRequestSent(senderId: String, receiverId: String): Boolean {
        val db = dbManager.readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM MatchRequest WHERE senderId = ? AND receiverId = ?",
            arrayOf(senderId, receiverId)
        )

        val matchExists = cursor.count > 0
        cursor.close()
        return matchExists
    }
}

