import android.database.Cursor
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

//멘토멘티 찾기 페이지의 스크롤뷰를 관리하는 클래스
class ProfileAdapter(val cursor: Cursor) :
    RecyclerView.Adapter<ProfileAdapter.ProfileViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_profile, parent, false)
        return ProfileViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProfileViewHolder, position: Int) {
        if (cursor.moveToPosition(position)) {
            val id = cursor.getString(cursor.getColumnIndexOrThrow("id"))
            val isMentor = cursor.getInt(cursor.getColumnIndexOrThrow("isMentor")) > 0
            val major = cursor.getString(cursor.getColumnIndexOrThrow("major"))
            holder.bind(id, isMentor, major)
        }
    }

    override fun getItemCount(): Int {
        return cursor.count
    }

    class ProfileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvId: TextView = itemView.findViewById(R.id.tvId)
        private val tvRole: TextView = itemView.findViewById(R.id.tvRole)
        private val tvMajor: TextView = itemView.findViewById(R.id.tvMajor)

        fun bind(id: String, isMentor: Boolean, major: String) {
            tvId.text = id
            tvRole.text = if (isMentor) "멘토" else "멘티"
            tvMajor.text = major
        }
    }
}
