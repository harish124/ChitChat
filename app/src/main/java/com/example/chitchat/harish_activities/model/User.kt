package com.example.chitchat.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class User(
    val uname:String="",
    val profile:String="",
    val cover:String="",
    val uid:String="",
    var status:String="",
    var lastMsg:String=""
):Parcelable
