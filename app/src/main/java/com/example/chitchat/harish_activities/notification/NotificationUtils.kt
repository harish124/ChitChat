package com.example.chitchat.harish_activities.notification

import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

data class Token(
    val token:String
)

data class Data(
    val user:String="",
    val icon:Int=0,
    val body:String="",
    val title:String="",
    val sent:String=""
)

data class NotificationSender(
    val data: Data,
    val to:String
)

class Client{
    var retrofit: Retrofit?=null

    fun getClient(url:String) : Retrofit?{
        if(retrofit==null){
            retrofit=Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        return retrofit
    }
}

data class MyResponse(
    var success:Int
)


public interface APIService{
    @Headers(
        "Content-Type: application/json",
        "Authorization: key=AAAAhmaVBwo:APA91bHFbeK3UxZaqmg05_PTIWYVe7tLX-K9lkLpgSu4rqQz1GIfzTUPlcO_5mVAWNblDDN1N0r7HIRQ6TkC_jw-7xA__t-NyH48e9x-DiwhES8TP_4UecCup2qYHZ-k7UYtblC64f6k"

    )

    @POST("fcm/send")
    fun sendNotification(@Body body:NotificationSender ): Call<MyResponse>
}