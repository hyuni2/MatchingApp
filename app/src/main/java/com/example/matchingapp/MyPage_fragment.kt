package com.example.matchingapp

import com.example.matchingapp.DBManager
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Button
import android.app.Activity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import java.io.IOException
import android.location.Geocoder
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import java.util.Locale


class MyPage_fragment : Fragment() {

    private val IMAGE_PICK_REQUEST_CODE = 1000
    private val PERMISSION_REQUEST_CODE = 1001

    // 네이버 지도
    private lateinit var tvUserLocation: TextView
    private lateinit var btnSetLocation: Button
    //

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_my_page_fragment, container, false)

        // TextView 연결 및 프로필 정보 표시
        val profileNameTextView: TextView = view.findViewById(R.id.ProfileName)
        val profileMajorTextView: TextView = view.findViewById(R.id.ProfileMajor)
        val imageView: ImageView = view.findViewById(R.id.imageView)
        val textView2: TextView = view.findViewById(R.id.textView2)
        val textView3: TextView = view.findViewById(R.id.textView3)
        //
        val tvMessageHistory: TextView = view.findViewById(R.id.tvMessageHistory)

        //네이버 지도
        tvUserLocation = view.findViewById(R.id.tvUserLocation) // 현재 위치 표시 텍스트뷰
        btnSetLocation = view.findViewById(R.id.btnSetLocation) // 위치 설정 버튼


        // 현재 로그인한 ID (SharedPreferences)
        val sharedPreferences =
            requireActivity().getSharedPreferences("UserPrefs", Activity.MODE_PRIVATE)
        val currentUserId = sharedPreferences.getString("loggedInUser", "정보 없음") ?: "정보 없음"


        val dbManager = DBManager(requireContext(), "MatchingAppDB", null, 1)
        val cursor = dbManager.getProfileById(currentUserId)

        textView2.setOnClickListener {
            // 계정탈퇴 액티비티 전환
            val intent = Intent(requireContext(), UserDeleteActivity::class.java)
            startActivity(intent)
        }

        textView3.setOnClickListener {
            // 프로필수정 액티비티 전환
            val intent = Intent(requireContext(), ProfileEditActivity::class.java)
            startActivity(intent)
        }

        tvMessageHistory.setOnClickListener {
            val intent = Intent(requireContext(), ChatListActivity::class.java)
            startActivity(intent)
        }

        // 📌 위치 설정 결과를 받는 새로운 방식
        val locationResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                val selectedLat = data?.getDoubleExtra("selectedLat", 0.0) ?: 0.0
                val selectedLng = data?.getDoubleExtra("selectedLng", 0.0) ?: 0.0

                // 📌 변환된 주소를 UI에 표시
                val address = getAddressFromLatLng(selectedLat, selectedLng)
                tvUserLocation.text = "현재 위치: $address"
            }
        }

// 📌 버튼 클릭 이벤트 수정
        btnSetLocation.setOnClickListener {
            val intent = Intent(requireContext(), MapActivity::class.java)
            locationResultLauncher.launch(intent) // 새로운 방식으로 액티비티 실행
        }


        if (cursor != null && cursor.moveToFirst()) {
            val profileName = cursor.getString(cursor.getColumnIndexOrThrow("name"))
            val profileMajor = cursor.getString(cursor.getColumnIndexOrThrow("major"))

            profileNameTextView.text = profileName.ifEmpty{"닉네임을 입력해주세요"}
            profileMajorTextView.text = profileMajor.ifEmpty{"전공을 입력해주세요"}
        } else {
            profileNameTextView.text = "닉네임을 입력해주세요"
            profileMajorTextView.text = "전공을 입력해주세요"
        }
        cursor?.close()

        // 이미지 선택 기능 추가
        imageView.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.READ_MEDIA_IMAGES
                )
                == PackageManager.PERMISSION_GRANTED
            ) {
                openGallery()
            } else {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.READ_MEDIA_IMAGES),
                    PERMISSION_REQUEST_CODE
                )
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(view.findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 📌 UI 요소 찾기
        val locationTextView = view.findViewById<TextView>(R.id.userLocationText)

        // 📌 사용자 위치 불러오기
        val userId = arguments?.getString("userId")
        if (userId != null) {
            val dbManager = DBManager(requireContext(), "MatchingAppDB", null, 1)
            val userLocation = dbManager.getUserLocation(userId)

            if (userLocation != null) {
                val address = getAddressFromLatLng(userLocation.latitude, userLocation.longitude)
                Log.d("ProfileDetail", "사용자 위치 업데이트: $address") // ✅ 디버깅 로그 추가
                locationTextView?.text = "현재 위치: $address"
            } else {
                Log.e("ProfileDetail", "사용자 위치 없음")
                locationTextView?.text = "현재 위치: 정보없음"
            }
        } else {
            Log.e("ProfileDetail", "userId 없음")
            locationTextView?.text = "현재 위치: 정보없음"
        }
        loadImage()
    }

    override fun onResume() {
        super.onResume()
        loadImage() // 프래그먼트가 화면에 보일 때마다 이미지 로드
        loadUserLocation()
    }

    private fun loadUserLocation() {
        val sharedPreferences = requireContext().getSharedPreferences("UserPrefs", AppCompatActivity.MODE_PRIVATE)
        val userId = sharedPreferences.getString("loggedInUser", null) // ✅ 로그인된 사용자 ID 가져오기

        if (userId != null) {
            val dbManager = DBManager(requireContext(), "MatchingAppDB", null, 1)
            val userLocation = dbManager.getUserLocation(userId)

            if (userLocation != null) {
                val address = getAddressFromLatLng(userLocation.latitude, userLocation.longitude)
                tvUserLocation.text = "현재 위치: $address" // ✅ UI 갱신
            } else {
                tvUserLocation.text = "현재 위치: 설정되지 않음" // 🔥 위치 정보가 없을 경우 기본값 설정
            }
        }
    }


    private fun saveImageUri(uri: Uri) {
        val sharedPreferences = requireActivity().getSharedPreferences("UserPrefs", Activity.MODE_PRIVATE)
        sharedPreferences.edit().putString("profileImageUri", uri.toString()).commit()
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, IMAGE_PICK_REQUEST_CODE)
    }

    private fun loadImage() {
        val sharedPreferences = requireActivity().getSharedPreferences("UserPrefs", Activity.MODE_PRIVATE)
        val imageUriString = sharedPreferences.getString("profileImageUri", null)

        if (imageUriString != null) {
            val imageUri = Uri.parse(imageUriString)

            val imageView = view?.findViewById<ImageView>(R.id.imageView)
            if (imageView != null) {
                Glide.with(this)
                    .load(imageUri)
                    .into(imageView)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // ✅ 프로필 이미지 로딩 및 유지
        if (requestCode == IMAGE_PICK_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            val imageUri: Uri = data.data!!
            saveImageUri(imageUri)

            val imageView = view?.findViewById<ImageView>(R.id.imageView)
            imageView?.let {
                Glide.with(this).load(imageUri).into(it)
            }
        }

        // ✅ MapActivity에서 위치 데이터 받아오기
        if (requestCode == LOCATION_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val selectedLat = data?.getDoubleExtra("selectedLat", 0.0) ?: 0.0
            val selectedLng = data?.getDoubleExtra("selectedLng", 0.0) ?: 0.0

            // ✅ 변환된 주소를 받아 UI에 적용
            val address = getAddressFromLatLng(selectedLat, selectedLng) // ✅ getAddressFromLatLng() 함수가 반환하도록 수정
            tvUserLocation.text = "현재 위치: $address" // ✅ 이제 Kotlin.Unit 문제 해결됨!
        }
    }

    // ✅ 위도, 경도로 주소 변환 (String 반환하도록 수정)
    private fun getAddressFromLatLng(lat: Double, lng: Double): String {
        val geocoder = Geocoder(requireContext(), Locale.KOREA)
        return try {
            val addresses = geocoder.getFromLocation(lat, lng, 1)
            if (!addresses.isNullOrEmpty()) {
                addresses[0].getAddressLine(0) // ✅ 변환된 주소 반환
            } else {
                "주소를 찾을 수 없습니다."
            }
        } catch (e: IOException) {
            e.printStackTrace()
            "주소 변환 오류"
        }
    }

    companion object {
        private const val LOCATION_REQUEST_CODE = 1001

        fun newInstance(): MyPage_fragment {
            return MyPage_fragment()
        }
    }
}
