package com.example.chitchat.model

data class Message(
    val msgId:String="",
    val fromId:String="",
    val toId:String="",
    var text:String="",
    val time:Long=12345678
)