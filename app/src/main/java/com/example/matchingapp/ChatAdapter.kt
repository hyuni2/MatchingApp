package com.example.matchingapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class ChatAdapter(private val currentUserId: String) : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    private var chatList: List<ChatMessage> = listOf()

    companion object {
        private const val VIEW_TYPE_SENT = 1
        private const val VIEW_TYPE_RECEIVED = 2
    }

    fun setChatList(chatList: List<ChatMessage>) {
        this.chatList = chatList
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return if (chatList[position].senderId == currentUserId) VIEW_TYPE_SENT else VIEW_TYPE_RECEIVED
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.chat_item, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val chatMessage = chatList[position]
        holder.bind(chatMessage, chatMessage.senderId == currentUserId)
    }

    override fun getItemCount(): Int = chatList.size

    class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageText: TextView = itemView.findViewById(R.id.messageText)

        fun bind(chatMessage: ChatMessage, isSentByUser: Boolean) {
            messageText.text = chatMessage.message

            val params = messageText.layoutParams as ViewGroup.MarginLayoutParams

            if (isSentByUser) {
                // 내 채팅: 오른쪽 정렬 + Mint 배경
                params.marginEnd = 16
                params.marginStart = 80
                messageText.setBackgroundResource(R.drawable.bg_chat_sent)
                messageText.textAlignment = View.TEXT_ALIGNMENT_VIEW_END
            } else {
                // 상대 채팅: 왼쪽 정렬 + Purple 배경
                params.marginStart = 16
                params.marginEnd = 80
                messageText.setBackgroundResource(R.drawable.bg_chat_received)
                messageText.textAlignment = View.TEXT_ALIGNMENT_VIEW_START
            }
            messageText.layoutParams = params
        }
    }
}