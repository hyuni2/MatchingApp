import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.example.matchingapp.FindMentoMenti_fragment
import com.example.matchingapp.MyPage_fragment
import com.example.matchingapp.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation_view)

        // 초기 프래그먼트 설정
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
                    replaceFragment(MyHistory_fragment())
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
}
