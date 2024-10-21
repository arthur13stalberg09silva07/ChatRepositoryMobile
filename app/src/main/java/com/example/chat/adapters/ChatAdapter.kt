package com.example.chat.adapters

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.chat.databinding.ItemContainerReceivedMessageBinding
import com.example.chat.databinding.ItemContainerSentMessageBinding
import com.example.chat.models.ChatMessage

    class ChatAdapter(private val chatMessages: List<ChatMessage>, private var receiverProfileImage: Bitmap, private val senderId: String ): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        private val VIEW_TYPE_SENT = 1
        private val VIEW_TYPE_RECEIVED = 2

        fun setReceiverProfileImage(bitmap: Bitmap){
            receiverProfileImage = bitmap
        }

        inner class SentMessageViewHolder(private val binding: ItemContainerSentMessageBinding): RecyclerView.ViewHolder(binding.root) {
            fun setData(message: ChatMessage) = with(binding){
                textMessage.text = message.message
                textDateTime.text = message.dateTime
            }

        }

        inner class ReceivedMessageViewHolder(private val binding: ItemContainerReceivedMessageBinding): RecyclerView.ViewHolder(binding.root){
            fun setData(message: ChatMessage) = with(binding){
                textMessage.text = message.message
                textDateTime.text = message.dateTime
                if(receiverProfileImage != null) {
                    imageProfile.setImageBitmap(receiverProfileImage)
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            if (viewType == VIEW_TYPE_SENT){
                return SentMessageViewHolder(
                    ItemContainerSentMessageBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }else{
                return ReceivedMessageViewHolder(
                    ItemContainerReceivedMessageBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
        }

        override fun getItemCount(): Int = chatMessages.size

        override fun getItemViewType(position: Int): Int {
            return if(chatMessages[position].senderId == senderId){
                VIEW_TYPE_SENT
            }else {
                VIEW_TYPE_RECEIVED
            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            if (getItemViewType(position) == VIEW_TYPE_SENT){
                (holder as SentMessageViewHolder).setData(chatMessages[position])
            }else{
                (holder as ReceivedMessageViewHolder).setData(chatMessages[position])
            }
        }
    }