package com.example.matchingapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.example.matchingapp.FindMentoMenti_fragment
import com.example.matchingapp.MyMatchingHistory_fragment
import com.example.matchingapp.MyPage_fragment
import com.example.matchingapp.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation_view)

        // 초기 프래그먼트 설정 (FindMentoMenti_fragment)
        replaceFragment(FindMentoMenti_fragment())

        // 네비게이션 클릭 리스너
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.findmentomenti -> {
                    replaceFragment(FindMentoMenti_fragment())
                    true
                }
                R.id.mypage -> {
                    replaceFragment(MyPage_fragment())
                    true
                }
                R.id.myhistory -> {
                    // 보낸 요청(true)와 받은 요청(false) 프래그먼트를 전환
                    replaceFragmentWithRequestFilter(true) // 보낸 요청
                    true
                }
                else -> false
            }
        }
    }

    // 프래그먼트를 교체하는 함수
    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    // MyMatchingHistory_fragment를 교체하고 isSentRequests 값을 설정하는 함수
    private fun replaceFragmentWithRequestFilter(isSentRequests: Boolean) {
        val fragment = MyMatchingHistory_fragment().apply {
            arguments = Bundle().apply {
                putBoolean("isSentRequests", isSentRequests)
            }
        }
        replaceFragment(fragment)
    }
}
