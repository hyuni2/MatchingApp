package com.example.matchingapp

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import android.widget.ImageButton
import com.example.matchingapp.ProfileAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class FindMentoMenti_fragment : Fragment() {

    private lateinit var dbManager: DBManager
    private lateinit var recyclerView: RecyclerView
    private lateinit var searchBar: EditText
    private lateinit var btnSearch: Button
    private lateinit var adapter: ProfileAdapter
    private lateinit var btnManageProfile: ImageButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_find_mento_menti, container, false)

        // DBManager 인스턴스 초기화
        dbManager = DBManager(requireContext(), "MatchingAppDB", null, 1)

        // "내 소개서 관리" 버튼 클릭 이벤트
        val btnManageProfile = view.findViewById<ImageButton>(R.id.btnManageProfile)
        btnManageProfile.setOnClickListener {
            val intent = Intent(requireContext(), ProfileEditActivity::class.java)
            startActivityForResult(intent, 100)
        }

        // RecyclerView 설정
        recyclerView = view.findViewById(R.id.rvMentoMentiList)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // 검색기능 설정
        searchBar = view.findViewById(R.id.searchBar)
        btnSearch = view.findViewById(R.id.btnSearch)

        // 프로필 목록 로드 함수
        fun loadProfiles(keyword: String) {
            val cursor = if (keyword.isEmpty()) {
                dbManager.getAllProfiles() // 🔥 전체 데이터 로드
            } else {
                dbManager.searchProfiles(keyword) // 🔥 검색 실행
            }

            adapter = ProfileAdapter(cursor) { profile ->
                navigateToDetailFragment(profile)
            }
            recyclerView.adapter = adapter
        }

        // 초기 프로필 목록 로드
        loadProfiles("")

        // 검색 버튼 클릭 시 검색 기능 실행
        btnSearch.setOnClickListener {
            val keyword = searchBar.text.toString().trim()
            loadProfiles(keyword)
        }

        return view
    }

    // 프로필 수정 후 결과 처리
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 100 && resultCode == AppCompatActivity.RESULT_OK) {
            // 프로필 수정 후 데이터 갱신
            val cursor = dbManager.getAllProfiles()
            adapter.swapCursor(cursor)
        }
    }

    // 프로필 상세 페이지로 이동
    private fun navigateToDetailFragment(profile: Profile) {
        if (profile == null) {
            Toast.makeText(requireContext(), "프로필 데이터를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show()
            return
        }
        val detailFragment = ProfileDetailFragment()

        // 데이터 전달
        val bundle = Bundle().apply {
            putString("name", profile.name)
            putString("isMentor", if (profile.isMentor == 1) "멘토" else "멘티")
            putString("major", profile.major)
            putString("userId", profile.userid)
            putString("intro", profile.intro)
        }
        detailFragment.arguments = bundle

        // Fragment 전환
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, detailFragment)
            .addToBackStack(null)
            .commit()
    }

    // onResume에서 데이터 갱신
    override fun onResume() {
        super.onResume()
        val cursor = dbManager.getAllProfiles()
        adapter.swapCursor(cursor)
    }

    // Fragment 뷰가 파괴될 때 리소스 해제
    override fun onDestroyView() {
        super.onDestroyView()
        if (::adapter.isInitialized) {
            adapter.cursor.close()
        }
    }
}

