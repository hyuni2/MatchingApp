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
import android.content.pm.PackageManager
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions



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

        /*네이버 지도
        tvUserLocation = view.findViewById(R.id.tvUserLocation) // 현재 위치 표시 텍스트뷰
        btnSetLocation = view.findViewById(R.id.btnSetLocation) // 위치 설정 버튼
        */

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

        /*네이버 지도
        // 기존에 저장된 위치 불러오기
        val storedLocation = sharedPreferences.getString("user_location", "현재 위치: 설정되지 않음")
        tvUserLocation.text = storedLocation

        // "위치 설정" 버튼 클릭 시 네이버 지도 화면 실행
        btnSetLocation.setOnClickListener {
            val intent = Intent(requireContext(), NaverMapActivity::class.java)
            startActivityForResult(intent, LOCATION_REQUEST_CODE)
        }
        */

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
        loadImage() // 프래그먼트가 다시 생성될 때 이미지 로드
    }

    override fun onResume() {
        super.onResume()
        loadImage() // 프래그먼트가 화면에 보일 때마다 이미지 로드
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
                    .into(imageView)  // Glide를 사용하여 이미지 표시
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // 프로필 이미지로딩 및 유지
        if (requestCode == IMAGE_PICK_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            val imageUri: Uri = data.data!!
            saveImageUri(imageUri)  // 선택한 이미지 URI 저장

            val imageView = view?.findViewById<ImageView>(R.id.imageView)
            if (imageView != null) {
                Glide.with(this)
                    .load(imageUri)
                    .into(imageView)  // Glide로 이미지 로딩
            }
        }

        // 네이버 지도에서 선택한 위치 받아오기
        if (requestCode == LOCATION_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val selectedLocation = data?.getStringExtra("selected_location") ?: return
            tvUserLocation.text = "현재 위치: $selectedLocation"

            // 위치를 SharedPreferences에 저장
            val sharedPreferences = requireActivity().getSharedPreferences("UserPrefs", Activity.MODE_PRIVATE)
            sharedPreferences.edit().putString("user_location", "현재 위치: $selectedLocation").apply()
        }
    }

    companion object {
        private const val LOCATION_REQUEST_CODE = 1001

        fun newInstance(): MyPage_fragment {
            return MyPage_fragment()
        }
    }
}
