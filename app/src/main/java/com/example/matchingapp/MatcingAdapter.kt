package com.example.matchingapp

import com.example.matchingapp.DBManager
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MatchingAdapter(private val context: Context, private var isSentRequests: Boolean) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var requests: MutableList<MatchRequest> = mutableListOf()

    // 데이터 설정 메서드
    fun setData(newRequests: List<MatchRequest>) {
        requests.clear()
        requests.addAll(newRequests)
        notifyDataSetChanged()
    }

    // 필터 변경 시 데이터 갱신 메서드
    fun setIsSentRequests(isSent: Boolean) {
        this.isSentRequests = isSent
        notifyDataSetChanged() // 필터가 변경될 때마다 UI 갱신
    }

    override fun getItemViewType(position: Int): Int {
        return if (isSentRequests) VIEW_TYPE_SENT else VIEW_TYPE_RECEIVED
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = if (viewType == VIEW_TYPE_SENT) {
            LayoutInflater.from(context).inflate(R.layout.match_request_sent, parent, false)
        } else {
            LayoutInflater.from(context).inflate(R.layout.match_request_received, parent, false)
        }
        return if (viewType == VIEW_TYPE_SENT) SentViewHolder(view) else ReceivedViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val request = requests[position]
        if (holder is SentViewHolder) {
            holder.bind(request)
        } else if (holder is ReceivedViewHolder) {
            holder.bind(request)
        }
    }

    override fun getItemCount(): Int = requests.size

    inner class SentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.nameTextView)
        private val majorTextView: TextView = itemView.findViewById(R.id.majorTextView)
        private val roleTextView: TextView = itemView.findViewById(R.id.roleTextView)
        private val statusTextView: TextView = itemView.findViewById(R.id.statusTextView)

        fun bind(request: MatchRequest) {
            // receiverId를 사용하여 이름을 가져오는 비동기 작업
            val userName = getUserNameById(request.receiverId)
            nameTextView.text = userName ?: "이름을 불러올 수 없습니다"
            majorTextView.text = request.receiverMajor // 'sent'인 경우 receiverMajor 표시
            roleTextView.text = when (request.isMentor) {
                1 -> "멘토"
                0 -> "멘티"
                else -> "알 수 없음" // 만약 1이나 0이 아닌 값이 들어오면 "알 수 없음"
            }
            statusTextView.text = when (request.status) {
                "completed" -> "매칭 완료"
                "failed" -> "매칭 실패"
                else -> "신청 완료"
            }
        }

        private fun getUserNameById(userId: String): String? {
            val dbManager = DBManager(context, "MatchingAppDB", null, 1)
            return dbManager.getUserNameById(userId)
        }
    }

    inner class ReceivedViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.nameTextView)
        private val majorTextView: TextView = itemView.findViewById(R.id.majorTextView)
        private val roleTextView: TextView = itemView.findViewById(R.id.roleTextView)
        private val acceptButton: ImageButton = itemView.findViewById(R.id.acceptButton)
        private val rejectButton: ImageButton = itemView.findViewById(R.id.rejectButton)

        fun bind(request: MatchRequest) {
            nameTextView.text = request.senderId
            majorTextView.text = request.senderMajor
            roleTextView.text = when (request.isMentor) {
                0 -> "멘토"
                1 -> "멘티"
                else -> "알 수 없음"
            }

            acceptButton.setOnClickListener {
                updateRequestStatus(request.id, "completed")
            }

            rejectButton.setOnClickListener {
                updateRequestStatus(request.id, "failed")
            }
        }

        private fun updateRequestStatus(requestId: Int, status: String) {
            val dbManager = DBManager(context, "MatchingAppDB", null, 1)
            dbManager.updateRequestStatus(requestId, status)
            // 상태 업데이트 후 UI 갱신
            setData(requests.map {
                if (it.id == requestId) it.copy(status = status) else it
            })
        }
    }

    companion object {
        private const val VIEW_TYPE_SENT = 1
        private const val VIEW_TYPE_RECEIVED = 2
    }
}
