import android.database.Cursor
import android.provider.ContactsContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.matchingapp.Profile
import com.example.matchingapp.R


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
        if (cursor.moveToPosition(position)) {
            val userid = cursor.getString(cursor.getColumnIndexOrThrow("userid"))
            val name = cursor.getString(cursor.getColumnIndexOrThrow("name"))
            val isMentor = cursor.getInt(cursor.getColumnIndexOrThrow("isMentor")) > 0
            val major = cursor.getString(cursor.getColumnIndexOrThrow("major"))
            val intro = cursor.getString(cursor.getColumnIndexOrThrow("intro"))
        //프로필  DB 에 소개글 추가 가능한지 문의하기.


            val profile = Profile(userid, name, isMentor, major, intro) // Profile 객체 생성
            holder.bind(profile)
            holder.itemView.setOnClickListener {
                onItemClick(profile) // 클릭 시 리스너 호출
            }
        }
    }

    override fun getItemCount(): Int {
        return cursor.count
    }

    class ProfileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.tvName)
        private val tvRole: TextView = itemView.findViewById(R.id.tvRole)
        private val tvMajor: TextView = itemView.findViewById(R.id.tvMajor)

        fun bind(profile: Profile) {
            tvName.text = profile.name
            tvRole.text = if (profile.isMentor) "멘토" else "멘티"
            tvMajor.text = profile.major
        }
    }
}

