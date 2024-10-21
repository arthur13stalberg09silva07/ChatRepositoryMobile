package com.example.chat.adapters

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.chat.databinding.ItemContainerUserBinding
import com.example.chat.listeners.UserListener
import com.example.chat.models.User


class UsersAdapter(private val users: List<User>, val listener: UserListener): RecyclerView.Adapter<UsersAdapter.UserViewHolder>() {
    inner class UserViewHolder(private val binding: ItemContainerUserBinding): RecyclerView.ViewHolder(binding.root) {
        fun setUserData(user: User) = with(binding){
            textName.text = user.name
            textEmail.text = user.email
            imageProfile.setImageBitmap(getUserImage(user.image.toString()))
            root.setOnClickListener{listener.onUserClicked(user)}
        }
        private fun getUserImage(encodedString: String): Bitmap {
            val bytes = Base64.decode(encodedString, Base64.DEFAULT)
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val itemContainerUserBinding = ItemContainerUserBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return UserViewHolder(itemContainerUserBinding)
    }

    override fun getItemCount(): Int = users.size

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.setUserData(users[position])
    }
}