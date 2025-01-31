package com.example.matchingapp

import com.example.matchingapp.DBManager
import ProfileAdapter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.EditText


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [FindMentoMenti_fragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class FindMentoMenti_fragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var adapter: ProfileAdapter

    // 검색기능
    private lateinit var dbManager: DBManager
    private lateinit var recyclerView: RecyclerView
    private lateinit var searchBar: EditText
    private lateinit var btnSearch: Button

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
        val view = inflater.inflate(R.layout.fragment_find_mento_menti, container, false)

        val dbManager = DBManager(requireContext(), "AppDatabase.db", null, 1)

        //"내 소개서 관리" 버튼 클릭 이벤트
        //내 소개서 관리 클릭 시 -> 마이페이지 이동, 마이페이지 "프로필 수정"으로 소개서 수정.
        val btnManageProfile = view.findViewById<Button>(R.id.btnManageProfile)

        val myPageFragment = MyPage_fragment()
        //Mypage로 이동할 프래그먼트 객체 생성

        //프래그먼트 이동 (마이페이지로)
        btnManageProfile.setOnClickListener{
            val fragmentManager: FragmentManager = getSupportFragmentManager()
            val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
            // Fragment 교체
            fragmentTransaction.replace(R.id.fragmentContainer, myPageFragment);
            fragmentTransaction.addToBackStack(null)

            // 변경 적용
            fragmentTransaction.commit();


        }

        //RecyclerView 초기화
        val recyclerView = view.findViewById<RecyclerView>(R.id.rvMentoMentiList)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // 검색기능
        searchBar = view.findViewById(R.id.searchBar)
        btnSearch = view.findViewById(R.id.btnSearch)

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

        loadProfiles("")

        btnSearch.setOnClickListener {
            val keyword = searchBar.text.toString().trim()
            loadProfiles(keyword)
        }
        //

        //cursor를 adapter에 연결
        val cursor = dbManager.getAllProfiles()
        adapter = ProfileAdapter(cursor){ profile ->
            navigateToDetailFragment(profile) // 클릭된 Profile 객체 전달
        }
        recyclerView.adapter = adapter

        return view

    }

    private fun getSupportFragmentManager(): FragmentManager {
        TODO("Not yet implemented")
    }

    private fun navigateToDetailFragment(profile: com.example.matchingapp.Profile) {

        if (profile == null) {
            Toast.makeText(requireContext(), "프로필 데이터를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show()
            return
        }
        val detailFragment = ProfileDetailFragment()

        // 데이터 전달
        val bundle = Bundle().apply {
            putString("name", profile.name)
            putString("role", if (profile.isMentor) "멘토" else "멘티")
            putString("major", profile.major)
            putString("intro", profile.intro)
        }
        detailFragment.arguments = bundle


        // Fragment 전환
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, detailFragment) // fragmentContainer는 메인 레이아웃의 ID
            .addToBackStack(null) // 뒤로 가기 지원
            .commit()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        //cursor 닫기
        adapter.cursor.close()
    }




    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment FindMentoMenti.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            FindMentoMenti_fragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}