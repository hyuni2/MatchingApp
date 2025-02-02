package com.example.matchingapp

import android.content.Context
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ChatActivity : AppCompatActivity() {

    private lateinit var dbManager: DBManager
    private lateinit var chatAdapter: ChatAdapter  // ✅ 올바르게 초기화
    private lateinit var recyclerView: RecyclerView
    private lateinit var editTextMessage: EditText
    private lateinit var sendButton: ImageButton
    private var senderId: String = ""
    private var receiverId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        // 채팅 상대 ID 가져오기
        receiverId = intent.getStringExtra("receiverId") ?: ""
        val receiverName = intent.getStringExtra("receiverName") ?: "알 수 없음"

        // 현재 로그인한 사용자 ID 가져오기
        val sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        senderId = sharedPreferences.getString("loggedInUser", "") ?: ""

        // UI 요소 연결
        val chatTitle = findViewById<TextView>(R.id.chatTitle)
        chatTitle.text = "채팅 - $receiverName"

        recyclerView = findViewById(R.id.recyclerViewChat)
        editTextMessage = findViewById(R.id.editTextMessage)
        sendButton = findViewById(R.id.sendButton)

        dbManager = DBManager(this, "MatchingAppDB", null, 1)

        // RecyclerView 설정
        chatAdapter = ChatAdapter(senderId)  // ✅ senderId 전달
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = chatAdapter  // ✅ 올바르게 RecyclerView에 연결

        // 채팅 내역 불러오기
        loadChatHistory()

        // 메시지 전송 버튼 클릭 이벤트
        sendButton.setOnClickListener {
            val message = editTextMessage.text.toString().trim()
            if (message.isNotEmpty()) {
                sendMessage(message)
                editTextMessage.text.clear()
            }
        }
    }

    private fun loadChatHistory() {
        val chatList = dbManager.getChatMessages(senderId, receiverId)
        chatAdapter.setChatList(chatList)  // ✅ 채팅 내역 업데이트
        recyclerView.scrollToPosition(chatList.size - 1) // 가장 최근 메시지로 이동
    }

    private fun sendMessage(message: String) {
        dbManager.insertChatMessage(senderId, receiverId, message)
        loadChatHistory() // UI 업데이트
    }
}