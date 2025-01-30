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
import android.app.Activity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment


class MyPage_fragment : Fragment() {

    private val IMAGE_PICK_REQUEST_CODE = 1000
    private val PERMISSION_REQUEST_CODE = 1001

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_my_page_fragment, container, false)

        // TextView 연결 및 프로필 정보 표시
        val profileNameTextView: TextView = view.findViewById(R.id.ProfileName)
        val profileMajorTextView: TextView = view.findViewById(R.id.ProfileMajor)
        val imageView: ImageView = view.findViewById(R.id.imageView)
        val textView3: TextView = view.findViewById(R.id.textView3)

        // 현재 로그인한 ID (SharedPreferences)
        val sharedPreferences = requireActivity().getSharedPreferences("UserPrefs", Activity.MODE_PRIVATE)
        val currentUserId = sharedPreferences.getString("loggedInUser", "정보 없음") ?: "정보 없음"


        val dbManager = DBManager(requireContext(), "MatchingAppDB", null, 1)
        val cursor = dbManager.getProfileById(currentUserId)


        textView3.setOnClickListener {
            // 액티비티 전환
            val intent = Intent(requireContext(), ProfileEditActivity::class.java)
            startActivity(intent)
        }


        if (cursor != null && cursor.moveToFirst()) {
            val profileName = cursor.getString(cursor.getColumnIndexOrThrow("name"))
            val profileMajor = cursor.getString(cursor.getColumnIndexOrThrow("major"))

            profileNameTextView.text = profileName
            profileMajorTextView.text = profileMajor
        } else {
            profileNameTextView.text = "닉네임을 입력해주세요"
            profileMajorTextView.text = "전공을 입력해주세요"
        }
        cursor?.close()

        // 이미지 선택 기능 추가
        imageView.setOnClickListener {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_MEDIA_IMAGES)
                == PackageManager.PERMISSION_GRANTED) {
                openGallery()
            } else {
                ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.READ_MEDIA_IMAGES), PERMISSION_REQUEST_CODE)
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(view.findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        return view
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, IMAGE_PICK_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMAGE_PICK_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            val imageUri: Uri = data.data!!
            view?.findViewById<ImageView>(R.id.imageView)?.setImageURI(imageUri)
        }
    }


    companion object {
        fun newInstance(): MyPage_fragment {
            return MyPage_fragment()
        }
    }


}
