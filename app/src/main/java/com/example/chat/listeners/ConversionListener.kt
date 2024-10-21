package com.example.chat.listeners

import com.example.chat.models.User

interface ConversionListener {
    fun onConversionClicked(user: User)
}