package com.example.matchingapp

import DBManager
import ProfileAdapter
import android.content.Intent
import android.os.Bundle
import android.provider.ContactsContract.Profile
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
z
/**
 * A simple [Fragment] subclass.
 * Use the [FindMentoMenti_fragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class FindMentoMenti_fragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var dbManager : DBManager
    private lateinit var adapter: ProfileAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    fun getSampleData(): List<String> {
        //샘플 데이터
        return listOf("멘토/멘티 1", "멘토/멘티 2", "멘토/멘티 3", "멘토/멘티 4", "멘토/멘티 5")
    }

    override fun onCreateView(

        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_find_mento_menti, container, false)

        //"내 소개서 관리" 버튼 클릭 이벤트
        //내 소개서 관리 클릭 시 -> 마이페이지 이동, 마이페이지 "프로필 수정"으로 소개서 수정.
        val btnManageProfile = view.findViewById<Button>(R.id.btnManageProfile)
        btnManageProfile.setOnClickListener{
            val intent = Intent(this, MyPage_fragment::class.java)
            startActivity(intent)
        }

        //RecyclerView 초기화
        val recyclerView = view.findViewById<RecyclerView>(R.id.rvMentoMentiList)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        //cursor를 adapter에 연결
        val cursor = dbManager.getAllProfiles()
        adapter = ProfileAdapter(cursor)
        recyclerView.adapter = adapter

        return view

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