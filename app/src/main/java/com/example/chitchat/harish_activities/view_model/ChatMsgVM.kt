package com.example.chitchat.harish_activities.view_model

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ChatMsgVM(): ViewModel() {
    val txtMsg=MutableLiveData<String>()
    init{

    }
}