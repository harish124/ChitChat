package com.example.chitchat.ui.message_acts

import android.app.Activity
import android.os.Bundle
import android.view.animation.OvershootInterpolator
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.chitchat.R
import com.example.chitchat.adapter.ChatMessageAdapter
import com.example.chitchat.databinding.ActivityChatMessageBinding
import com.example.chitchat.model.Message
import com.example.chitchat.model.User
import com.example.chitchat.ui.FirstScreen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import frame_transition.Transition

import jp.wasabeef.recyclerview.adapters.ScaleInAnimationAdapter
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator

class ChatMessageActivity : Activity() {

    private var binding:ActivityChatMessageBinding?=null
    private val mAuth = FirebaseAuth.getInstance()
    private val database= FirebaseDatabase.getInstance()
    private val transition=Transition(this)
    private val messages= arrayListOf<Message>()
    private val mAdapter= ChatMessageAdapter(messages)

    var user:User?=null


    private fun fetchUser() = intent.getParcelableExtra<User>("UserObj")
    private fun setStatus() = database.getReference("Users/${mAuth.uid}/status").setValue("Online")
    private fun configRecyclerView() {
        val lm=LinearLayoutManager(this@ChatMessageActivity)

        lm.stackFromEnd=true
        binding?.recyclerView?.apply {
            setHasFixedSize(true)
            layoutManager=lm
            adapter=ScaleInAnimationAdapter(mAdapter).apply{
                setFirstOnly(false)
                setDuration(1000)
                setHasStableIds(false)
                setInterpolator(OvershootInterpolator(.100f))
            }
            itemAnimator= SlideInUpAnimator(OvershootInterpolator(1f))
        }
    }

    private fun init(){
        user=fetchUser()
        setStatus()

        binding!!.userName.text=user?.uname
        Glide.with(this)
            .load(user?.profile)
            .centerCrop()
            .into(binding!!.profileImage)
        mAdapter.toUserImgUrl=user!!.profile

        configRecyclerView()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=DataBindingUtil.setContentView(this,R.layout.activity_chat_message)

        init()

        binding!!.sendBtn.setOnClickListener{
            onSendBtnPressed()
        }

        binding!!.backBtn.setOnClickListener{
            transition.goTo(FirstScreen::class.java)
            finish()
        }

        listenForMessages(user?.uid!!)

    }

    private fun onSendBtnPressed() {
        sendToFirebase()
        binding!!.txtMsg.setText("")
        binding!!.recyclerView.scrollToPosition(messages.size)

        val seenRef=database.getReference("Seen/${mAuth.uid}/${user!!.uid}")
        seenRef.child("hasSeen")
            .setValue(0)
            .addOnSuccessListener {
                println("Seen set successfully")
            }
    }

    private fun listenForMessages(toUserUid:String) {
        database.getReference("messages")
            .addValueEventListener(
                object :ValueEventListener{
                    override fun onCancelled(p0: DatabaseError) {
                        TODO("Not yet implemented")
                    }

                    override fun onDataChange(p0: DataSnapshot) {
                        var i=0
                        messages.clear()
                        mAdapter.notifyDataSetChanged()
                        p0.children.forEach{
                            if(it.exists()){
                                val msg=it.getValue(Message::class.java) ?: Message(text="Error")
                                if((msg.toId==toUserUid) and (msg.fromId==mAuth.uid)) {
                                    println("From me: $msg")
                                    messages.add(msg)
                                    mAdapter.notifyItemInserted(i)
                                    i += 1
                                }
                                else if((msg.fromId==toUserUid) and (msg.toId==mAuth.uid)) {
                                    println("From him: $msg")
                                    messages.add(msg)
                                    mAdapter.notifyItemInserted(i)
                                    i += 1
                                }
                            }
                        }
                    }

                })


    }

    private fun constructMessage(key:String=""):Message{
        return Message(
            key,
            mAuth.uid!!,
            user!!.uid,
            binding!!.txtMsg.text.toString(),
            System.currentTimeMillis()/1000
        )
    }

    private fun sendToFirebase() {
        val ref=database.getReference("messages").push()
        val msg=constructMessage(ref.key.toString())
        if(msg.text.isEmpty()){
            return
        }
        ref.setValue(msg)
        .addOnFailureListener { e->
            println("Error in sending msg: ${e.message}")
        }

        //setting Last Message
        setLastMsg(msg)
    }

    private fun setLastMsg(msg: Message){
        val lastMsgRef=database.getReference("last_messages/${mAuth.uid}/${user!!.uid}/lastMsg")
        val lastMsgRef2=database.getReference("last_messages/${user!!.uid}/${mAuth.uid}/lastMsg")

        lastMsgRef.setValue(msg.text)
        lastMsgRef2.setValue(msg.text)
    }

}



