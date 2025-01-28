package com.example.matchingapp

import DBManager
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MatchingAdapter(private val context: Context, private val isSentRequests: Boolean) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var requests: List<MatchRequest> = listOf()

    fun setData(data: List<MatchRequest>) {
        this.requests = data
        notifyDataSetChanged()
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
            nameTextView.text = request.senderId // 예시로 senderId를 이름으로 사용
            majorTextView.text = request.receiverId // 예시로 receiverId를 전공으로 사용
            roleTextView.text = if (request.isMentor) "멘토" else "멘티" // 멘토/멘티 표시
            statusTextView.text = when (request.status) {
                "completed" -> "매칭 완료"
                "failed" -> "매칭 실패"
                else -> "신청 완료"
            }
        }
    }

    inner class ReceivedViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.nameTextView)
        private val majorTextView: TextView = itemView.findViewById(R.id.majorTextView)
        private val introTextView: TextView = itemView.findViewById(R.id.introTextView)
        private val acceptButton: Button = itemView.findViewById(R.id.acceptButton)
        private val rejectButton: Button = itemView.findViewById(R.id.rejectButton)

        fun bind(request: MatchRequest) {
            nameTextView.text = request.senderId // 예시로 senderId를 이름으로 사용
            majorTextView.text = request.receiverId // 예시로 receiverId를 전공으로 사용
            introTextView.text = "자기 소개 텍스트" // 예시로 자기 소개 텍스트를 사용

            acceptButton.setOnClickListener {
                updateRequestStatus(request.id, "completed")
            }

            rejectButton.setOnClickListener {
                updateRequestStatus(request.id, "failed")
            }
        }

        private fun updateRequestStatus(requestId: Int, status: String) {
            val dbManager = DBManager(context, "MatchingDB", null, 1)
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