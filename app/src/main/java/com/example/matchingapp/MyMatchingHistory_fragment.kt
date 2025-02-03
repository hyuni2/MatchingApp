package com.example.matchingapp

import android.app.Activity
import com.example.matchingapp.DBManager
import android.os.Bundle
import android.util.Log
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
    private var isSentRequests = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)

            isSentRequests = it.getBoolean("isSentRequests", true)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.mymatchinghistory, container, false)
        dbManager = DBManager(requireContext(), "MatchingAppDB", null, 1)

        sendFilterChip = view.findViewById(R.id.sendfilter)
        receiveFilterChip = view.findViewById(R.id.recievefilter)
        recyclerView = view.findViewById(R.id.mysendmatch)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        loadMatchRequests(isSentRequests)

        sendFilterChip.setOnClickListener {
            isSentRequests = true
            loadMatchRequests(isSentRequests)
            Log.d("MatchingFragment", "isSentRequests = $isSentRequests")
        }

        receiveFilterChip.setOnClickListener {
            isSentRequests = false
            loadMatchRequests(isSentRequests)
            Log.d("MatchingFragment", "isSentRequests = $isSentRequests")
        }

        return view
    }

    private fun loadMatchRequests(isSentRequests: Boolean) {
        // 현재 로그인한 ID (SharedPreferences)
        val sharedPreferences =
            requireActivity().getSharedPreferences("UserPrefs", Activity.MODE_PRIVATE)
        val userId = sharedPreferences.getString("loggedInUser", "정보 없음") ?: "정보 없음"

        val cursor = if (isSentRequests) {
            dbManager.getSentRequests(userId)
        } else {
            dbManager.getReceivedRequests(userId)
        }

        // 데이터를 DB에서 가져오기
        val requests = mutableListOf<MatchRequest>()
        while (cursor.moveToNext()) {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
            val senderId = cursor.getString(cursor.getColumnIndexOrThrow("senderId"))
            val receiverId = cursor.getString(cursor.getColumnIndexOrThrow("receiverId"))
            val status = cursor.getString(cursor.getColumnIndexOrThrow("status"))
            val isMentor = cursor.getInt(cursor.getColumnIndexOrThrow("isMentor"))
            val senderMajor = cursor.getString(cursor.getColumnIndexOrThrow("senderMajor"))
            val receiverMajor = cursor.getString(cursor.getColumnIndexOrThrow("receiverMajor"))
            requests.add(MatchRequest(id, senderId, receiverId, status, isMentor, senderMajor, receiverMajor))
        }
        cursor.close()

        // 기존의 MatchingAdapter를 재사용하여 데이터만 업데이트
        if (::matchingAdapter.isInitialized) {
            matchingAdapter.setIsSentRequests(isSentRequests)
            matchingAdapter.setData(requests)
        } else {
            matchingAdapter = MatchingAdapter(requireContext(), isSentRequests)
            recyclerView.adapter = matchingAdapter
            matchingAdapter.setData(requests)
        }
    }
}
