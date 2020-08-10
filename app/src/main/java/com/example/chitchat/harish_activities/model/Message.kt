package com.example.chitchat.harish_activities.model

data class Message(
    val msgId:String="",
    val fromId:String="",
    val toId:String="",
    val text:String="",
    var seen:Int=0,
    val date:String="",
    val time:String="",
    val delete: Int =0,
    val replyMsg:String="",
    val replyPos:Int=-1

)