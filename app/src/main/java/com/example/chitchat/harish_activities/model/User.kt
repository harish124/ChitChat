package com.example.chitchat.harish_activities.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class User(
    val uname:String="",
    val profile:String="",
    val cover:String="",
    val uid:String="",
    var status:String="",
    var lastMsg:String="",
    val deviceToken:String=""
):Parcelable

@Parcelize
data class Group(
    val gid:String="",
    val gname:String="",
    val profile: String="",
    var lastMsg: String=""
):Parcelable
