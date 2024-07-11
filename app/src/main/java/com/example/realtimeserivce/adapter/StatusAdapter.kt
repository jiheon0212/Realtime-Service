package com.example.realtimeserivce.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.realtimeserivce.data.CurrentStatus
import com.example.realtimeserivce.databinding.CurrentStatusOfflineBinding
import com.example.realtimeserivce.databinding.CurrentStatusOnlineBinding

class StatusAdapter(private var currentStatus: MutableList<CurrentStatus>, private val onItemClick: (String) -> Unit): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val VIEW_TYPE_ON = 1
    private val VIEW_TYPE_OFF = 2

    inner class StatusOnlineViewHolder(val binding: CurrentStatusOnlineBinding): RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    val uid = currentStatus[adapterPosition].user
                    onItemClick(uid!!)
                }
            }
        }
    }
    inner class StatusOfflineViewHolder(val binding: CurrentStatusOfflineBinding): RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    val uid = currentStatus[adapterPosition].user
                    onItemClick(uid!!)
                }
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val statusData = currentStatus[position]
        val (user, _) = statusData

        if (holder.itemViewType == VIEW_TYPE_ON){
            (holder as StatusOnlineViewHolder).binding.apply {
                tvUserName.text = user
            }
        } else {
            (holder as StatusOfflineViewHolder).binding.apply {
                tvUserName.text = user
            }
        }
    }

    override fun getItemCount(): Int = currentStatus.size
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val onBinding = CurrentStatusOnlineBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val offBinding = CurrentStatusOfflineBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return if (viewType == VIEW_TYPE_ON) {
            StatusOnlineViewHolder(onBinding)
        } else {
            StatusOfflineViewHolder(offBinding)
        }
    }
    override fun getItemViewType(position: Int): Int {
        return if (currentStatus[position].status == "online") VIEW_TYPE_ON else VIEW_TYPE_OFF
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateCurrentUserList(newList: MutableList<CurrentStatus>) {
        currentStatus = newList
        notifyDataSetChanged()
    }
}