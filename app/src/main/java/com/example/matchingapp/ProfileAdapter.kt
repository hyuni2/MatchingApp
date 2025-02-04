package com.example.matchingapp

import android.database.Cursor
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView



class ProfileAdapter(
    var cursor: Cursor,
    private val onItemClick: (Profile) -> Unit // 클릭 리스너 추가
) : RecyclerView.Adapter<ProfileAdapter.ProfileViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_profile, parent, false)
        return ProfileViewHolder(view)
    }

    fun swapCursor(newCursor: Cursor) {
        if (cursor != null && !cursor.isClosed) { // 현재 커서가 유효하면 닫기
            cursor.close()
        }
        cursor = newCursor  // 새 커서로 교체
        notifyDataSetChanged()  // 데이터 변경 알리기
    }

    override fun onBindViewHolder(holder: ProfileViewHolder, position: Int) {
        // 커서를 현재 포지션으로 이동
        if (cursor.moveToPosition(position)) {
            val userIdIndex = cursor.getColumnIndex("userid")
            val nameIndex = cursor.getColumnIndex("name")
            val isMentorIndex = cursor.getColumnIndex("isMentor")
            val majorIndex = cursor.getColumnIndex("major")
            val introIndex = cursor.getColumnIndex("intro")

            // 안전하게 데이터 가져오기 (컬럼이 존재하는지 확인 후 값 가져오기)
            val userid = if (userIdIndex != -1) cursor.getString(userIdIndex) else ""
            val name = if (nameIndex != -1) cursor.getString(nameIndex) else "이름 없음"
            val isMentor = if (isMentorIndex != -1) cursor.getInt(isMentorIndex) else 0
            val major = if (majorIndex != -1) cursor.getString(majorIndex) else "전공 없음"
            val intro = if (introIndex != -1) cursor.getString(introIndex) else "소개 없음"

            // Profile 객체 생성 후 ViewHolder에 바인딩
            val profile = Profile(userid, name, isMentor, major, intro)
            holder.bind(profile)

            // 아이템 클릭 시 해당 프로필 정보를 전달하는 클릭 이벤트 설정
            holder.itemView.setOnClickListener {
                onItemClick(profile)
            }
        }
    }

    override fun getItemCount(): Int {
        return cursor.count
    }

    // 프로필 정보를 표시하는 뷰 홀더 클래스
    class ProfileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.tvName)
        private val tvRole: TextView = itemView.findViewById(R.id.tvRole)
        private val tvMajor: TextView = itemView.findViewById(R.id.tvMajor)
        private val imageView2: ImageView = itemView.findViewById(R.id.imageView2)

        // 프로필 정보를 뷰에 바인딩
        fun bind(profile: Profile) {
            tvName.text = profile.name
            tvRole.text = if (profile.isMentor==1) "멘토" else "멘티"
            tvMajor.text = profile.major
            imageView2.setImageResource(R.drawable.profileframe)
        }
    }
}
