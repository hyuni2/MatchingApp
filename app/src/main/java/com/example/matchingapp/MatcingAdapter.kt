package com.example.matchingapp

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class MatchingAdapter(private val context: Context) :
    RecyclerView.Adapter<MatchingAdapter.ViewHolder>() {

    private var requests: List<MatchRequest> = listOf()

    fun setData(data: List<MatchRequest>) {
        this.requests = data
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_match_request, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val request = requests[position]
        holder.bind(request)
    }

    override fun getItemCount(): Int = requests.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(request: MatchRequest) {
            // 데이터를 아이템 뷰에 바인딩
        }
    }
}
