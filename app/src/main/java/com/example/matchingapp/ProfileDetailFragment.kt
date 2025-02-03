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
import android.content.Intent
import android.location.Geocoder
import okio.IOException
import java.util.Locale

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
        val role = arguments?.getString("role") // 'role'을 그대로 사용
        val major = arguments?.getString("major")
        val intro = arguments?.getString("intro")

        // UI 연결 및 데이터 표시
        view.findViewById<TextView>(R.id.nameText).text = name
        view.findViewById<TextView>(R.id.roleText).text = role
        view.findViewById<TextView>(R.id.majorText).text = major
        view.findViewById<TextView>(R.id.introText).text = intro

        val applyButton = view.findViewById<Button>(R.id.applyButton)

        // 쪽지하기 버튼 찾기
        val btnMessage = view.findViewById<Button>(R.id.btnMessage)

        // 현재 사용자 ID 및 상대방 ID 가져오기
        val senderId = getCurrentUserId()
        val receiverId = name?.let { getUserIdByName(it) }

        if (receiverId != null) {
            when {
                senderId == receiverId -> {
                    applyButton.isEnabled = false
                    applyButton.text = "자기 자신에게 요청 불가"

                    // 쪽지하기 버튼 비활성
                    btnMessage.isEnabled = false
                    btnMessage.text = "쪽지 불가"
                }
                requestSent(senderId, receiverId) -> {
                    applyButton.isEnabled = false
                    applyButton.text = "요청 완료"

                    // 쪽지하기 버튼 활성
                    btnMessage.isEnabled = true
                    btnMessage.text = "쪽지하기"
                }
                else -> {
                    applyButton.isEnabled = true
                    applyButton.text = "매칭 요청"

                    // 쪽지하기 버튼 활성
                    btnMessage.isEnabled = true
                    btnMessage.text = "쪽지하기"
                }
            }


            applyButton.setOnClickListener {
                if (receiverId != null) {
                    val isMentor = if (role == "멘토") 1 else 0
                    val senderMajor = getUserMajorById(senderId) // 현재 사용자 전공 (String?)
                    val receiverMajor = major // 전달받은 전공 (String?)
                    sendMatchRequest(receiverId, isMentor, senderMajor, receiverMajor, applyButton) // 'role'이 "멘토"일 때 1, 아니면 0 전달
                }
            }


            // 쪽지하기 버튼 클릭시
            btnMessage.setOnClickListener {
                val intent = Intent(requireContext(), ChatActivity::class.java)
                intent.putExtra("receiverId", receiverId) // 채팅할 상대 ID
                intent.putExtra("receiverName", name) // 상대 이름 전달
                startActivity(intent)
            }
        } else {
            applyButton.isEnabled = false
            applyButton.text = "요청 불가"

            // 쪽지하기 버튼 비활성
            btnMessage.isEnabled = false
            btnMessage.text = "쪽지 불가"
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 📌 선택된 프로필의 사용자 ID 가져오기
        val userId = arguments?.getString("userId")

        if (userId != null) {
            val dbManager = DBManager(requireContext(), "MatchingAppDB", null, 1)
            val userLocation = dbManager.getUserLocation(userId)

            if (userLocation != null) {
                // 📌 UI에 위치 데이터 표시
                val address = getAddressFromLatLng(userLocation.latitude, userLocation.longitude)
                val locationTextView: TextView = view.findViewById(R.id.userLocationText) // 🔥 onViewCreated에서는 view 사용
                locationTextView.text = "현재 위치: $address"
            }
        }
    }

    // 📌 위도, 경도를 주소로 변환하는 함수
    private fun getAddressFromLatLng(lat: Double, lng: Double): String {
        val geocoder = Geocoder(requireContext(), Locale.KOREA)
        return try {
            val addresses = geocoder.getFromLocation(lat, lng, 1)
            if (!addresses.isNullOrEmpty()) {
                addresses[0].getAddressLine(0) // 전체 주소 반환
            } else {
                "주소를 찾을 수 없음"
            }
        } catch (e: IOException) {
            e.printStackTrace()
            "주소 변환 오류"
        }
    }

    // receiverId를 직접 매개변수로 받도록 수정
    private fun sendMatchRequest(receiverId: String, isMentor: Int, senderMajor: String?, receiverMajor: String?, button: Button) {
        if (receiverId.isEmpty()) {
            Toast.makeText(requireContext(), "잘못된 프로필 정보입니다.", Toast.LENGTH_SHORT).show()
            return
        }

        val senderId = getCurrentUserId()

        if (senderId == receiverId) {
            Toast.makeText(requireContext(), "자기 자신에게는 매치 요청을 보낼 수 없습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        // senderMajor, receiverMajor null처리
        val finalSenderMajor = senderMajor ?: "기본 전공" // 기본값으로 대체
        val finalReceiverMajor = receiverMajor ?: "기본 전공" // 기본값으로 대체

        // 매칭 요청을 DB에 삽입
        val success = dbManager.insertMatchRequest(senderId, receiverId, isMentor, finalSenderMajor, finalReceiverMajor)
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

    // 사용자 ID로 전공 가져오기
    private fun getUserMajorById(userId: String): String? {
        val db = dbManager.readableDatabase
        val cursor = db.rawQuery("SELECT major FROM Profile WHERE userid=?", arrayOf(userId))
        var major: String? = null
        if (cursor.moveToFirst()) {
            major = cursor.getString(0)
        }
        cursor.close()
        return major
    }

    // 요청을 보냈는지 확인하는 함수
    private fun requestSent(senderId: String, receiverId: String): Boolean {
        val db = dbManager.readableDatabase
        val cursor = db.rawQuery(
            "SELECT COUNT(*) FROM MatchRequest WHERE senderId = ? AND receiverId = ?",
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
