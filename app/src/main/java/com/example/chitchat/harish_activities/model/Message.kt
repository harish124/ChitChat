package com.example.chitchat.model

data class Message(
    val msgId:String="",
    val fromId:String="",
    val toId:String="",
    var text:String="",
    var seen:Int=0,
    val date:String="",
    val time:String=""
)