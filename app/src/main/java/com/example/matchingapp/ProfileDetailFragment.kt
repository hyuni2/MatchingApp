package com.example.matchingapp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ProfileDetailFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ProfileDetailFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

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
        // 레이아웃 연걸
        val view = inflater.inflate(R.layout.fragment_profile_detail, container, false)

        //전달받은 데이터 처리
        val name = arguments?.getString("name")
        val role = arguments?.getString("role")
        val major = arguments?.getString("major")
        val intro = arguments?.getString("intro")

        // UI 연결 및 데이터 표시
        view.findViewById<TextView>(R.id.nameText).text = name
        view.findViewById<TextView>(R.id.roleText).text = role
        view.findViewById<TextView>(R.id.majorText).text = major
        view.findViewById<TextView>(R.id.introText).text = intro

        // 버튼 동작 추가
        view.findViewById<Button>(R.id.applyButton).setOnClickListener {
            // 신청하기 버튼 클릭 로직
        }
        return view
    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ProfileDetailFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ProfileDetailFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}