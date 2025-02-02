package com.example.matchingapp

import android.content.Intent
import android.os.Bundle
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity

class ChatListActivity : AppCompatActivity() {

    private lateinit var dbManager: DBManager
    private lateinit var chatListView: ListView
    private lateinit var chatListAdapter: ArrayAdapter<String>
    private var chatPartners: List<String> = listOf()
    private var currentUserId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_list)

        // 현재 로그인한 사용자 ID 가져오기
        val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        currentUserId = sharedPreferences.getString("loggedInUser", "") ?: ""

        // UI 요소 연결
        chatListView = findViewById(R.id.chatListView)
        dbManager = DBManager(this, "MatchingAppDB", null, 1)

        // 대화 상대 목록 가져오기
        loadChatPartners()

        // 클릭 시 해당 채팅방 열기
        chatListView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            val selectedUserId = chatPartners[position]
            val intent = Intent(this, ChatActivity::class.java).apply {
                putExtra("receiverId", selectedUserId)
                putExtra("receiverName", selectedUserId) // 닉네임 대신 ID 사용
            }
            startActivity(intent)
        }

        val closeButton = findViewById<ImageButton>(R.id.closeChatListButton)
        closeButton.setOnClickListener {
            finish() // 창 닫기
        }
    }

    private fun loadChatPartners() {
        chatPartners = dbManager.getChatPartners(currentUserId)
        chatListAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, chatPartners)
        chatListView.adapter = chatListAdapter
    }
}