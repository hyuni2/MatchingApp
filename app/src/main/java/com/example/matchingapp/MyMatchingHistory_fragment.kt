package com.example.matchingapp

import com.example.matchingapp.DBManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class MyMatchingHistory_fragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var dbManager: DBManager
    private lateinit var sendFilterChip: Chip
    private lateinit var receiveFilterChip: Chip
    private lateinit var recyclerView: RecyclerView
    private lateinit var matchingAdapter: MatchingAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.mymatchinghistory, container, false)
        dbManager = DBManager(requireContext(), "MatchingDB", null, 1)

        sendFilterChip = view.findViewById(R.id.sendfilter)
        receiveFilterChip = view.findViewById(R.id.recievefilter)
        recyclerView = view.findViewById(R.id.mysendmatch)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        loadMatchRequests(isSentRequests = true)

        sendFilterChip.setOnClickListener {
            loadMatchRequests(isSentRequests = true)
        }

        receiveFilterChip.setOnClickListener {
            loadMatchRequests(isSentRequests = false)
        }

        return view
    }
    private fun loadMatchRequests(isSentRequests: Boolean) {
        val userId = "currentUserId" // 현재 로그인된 사용자 ID (추가 구현 필요)
        val cursor = if (isSentRequests) {
            dbManager.getSentRequests(userId)
        } else {
            dbManager.getReceivedRequests(userId)
        }

        val requests = mutableListOf<MatchRequest>()
        while (cursor.moveToNext()) {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
            val senderId = cursor.getString(cursor.getColumnIndexOrThrow("senderId"))
            val receiverId = cursor.getString(cursor.getColumnIndexOrThrow("receiverId"))
            val status = cursor.getString(cursor.getColumnIndexOrThrow("status"))
            val isMentor = cursor.getInt(cursor.getColumnIndexOrThrow("isMentor")) == 1 // 멘토/멘티 정보
            requests.add(MatchRequest(id, senderId, receiverId, status, isMentor))
        }
        cursor.close()

        // 기존의 MatchingAdapter를 재사용하여 데이터만 업데이트
        if (::matchingAdapter.isInitialized) {
            matchingAdapter.setData(requests)
        } else {
            matchingAdapter = MatchingAdapter(requireContext(), isSentRequests)
            recyclerView.adapter = matchingAdapter
            matchingAdapter.setData(requests)
        }
    }}
