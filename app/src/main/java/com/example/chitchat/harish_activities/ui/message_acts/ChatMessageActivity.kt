package com.example.chitchat.harish_activities.ui.message_acts

import android.app.Activity
import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.animation.OvershootInterpolator
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.chitchat.R
import com.example.chitchat.databinding.ActivityChatMessageBinding
import com.example.chitchat.harish_activities.adapter.ChatMessageAdapter
import com.example.chitchat.harish_activities.ui.FirstScreen
import com.example.chitchat.harish_activities.view_model.ChatMsgVM
import com.example.chitchat.model.Message
import com.example.chitchat.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import frame_transition.Transition
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator
import jp.wasabeef.recyclerview.adapters.ScaleInAnimationAdapter
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator
import print.Print
import java.text.SimpleDateFormat
import java.util.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity



class ChatMessageActivity : AppCompatActivity() {

    private var binding: ActivityChatMessageBinding?=null
    private val mAuth = FirebaseAuth.getInstance()
    private val database= FirebaseDatabase.getInstance()
    private val transition=Transition(this)
    private val messages= arrayListOf<Message>()
    private val mAdapter= ChatMessageAdapter(messages)
    private val p= Print(this)
    private var vm:ChatMsgVM?=null
    var user:User?=null
    private var pos=-1
    private var swipedMsg:Message?=null


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
        vm=ViewModelProvider(this).get(ChatMsgVM::class.java)
        binding!!.userName.text=user?.uname
        Glide.with(this)
            .load(user?.profile)
            .centerCrop()
            .into(binding!!.profileImage)
        mAdapter.toUserImgUrl=user!!.profile
        mAdapter.toUserUid=user!!.uid

        binding!!.vm=vm
        configRecyclerView()
        setItemTouchHelper()
        listenOnlineStatus()
        setObservers()

        observeUserTyping()
    }

    private fun listenOnlineStatus() {
        database.getReference("Users/${user?.uid}/status")
            .addValueEventListener(object:ValueEventListener{
                override fun onCancelled(p0: DatabaseError) {
                    TODO("Not yet implemented")
                }

                override fun onDataChange(p0: DataSnapshot) {
                    if(p0.exists()){
                        if(p0.value.toString()=="Online"){
                            binding!!.onlineBall.visibility= View.VISIBLE
                        }
                        else{
                            binding!!.onlineBall.visibility= View.INVISIBLE
                        }
                    }
                }

            })
    }

    private fun observeUserTyping() {
        val ref=database.getReference("last_messages/${user?.uid}/${mAuth.uid}/isTyping")

        ref.addValueEventListener(object:ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    if(p0.value.toString()=="1"){
                        binding!!.userName.text="${user?.uname} is Typing..."
                        binding!!.userName.setTextColor(Color.GREEN)
                    }
                    else{
                        binding!!.userName.text="${user?.uname}"
                        binding!!.userName.setTextColor(Color.WHITE)
                    }
                }
            }

        })
    }

    private fun setObservers() {
        vm!!.txtMsg.observe(this, androidx.lifecycle.Observer{msg->
            val ref=database.getReference("last_messages/${mAuth.uid}/${user?.uid}/isTyping")
            if (msg.isBlank()) {
                ref.setValue(0)
            }
            else{
                ref.setValue(1)
            }
        })
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

        val seenRef=database.getReference("last_messages/${user!!.uid}/${mAuth.uid}/hasSeen")
        seenRef.setValue(0)
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

    private fun setItemTouchHelper() {
        val mIth = ItemTouchHelper(
            object : ItemTouchHelper.SimpleCallback(
                0, ItemTouchHelper.LEFT
            ) {
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    TODO("Not yet implemented")
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    pos=viewHolder.adapterPosition
                    try{
                        println("Swipe Worked")
                    }
                    catch (e: Exception){
                        //p.fprintf("Error: ${e.message}")
                        println("Error: ${e.message}")
                    }
                }

                override fun onChildDraw(
                    c: Canvas,
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    dX: Float,
                    dY: Float,
                    actionState: Int,
                    isCurrentlyActive: Boolean
                ) {

                    if(actionState==ItemTouchHelper.ACTION_STATE_SWIPE) {

                        pos=viewHolder.adapterPosition
                        val date=messages[pos].date
                        viewHolder.itemView.translationX = dX / 4
                        RecyclerViewSwipeDecorator.Builder(
                            c,
                            recyclerView,
                            viewHolder,
                            dX,
                            dY,
                            actionState,
                            isCurrentlyActive
                        )
                            .addBackgroundColor(
                                R.color.colorPrimary
                            )
                            .addSwipeLeftLabel(date+" "+messages[pos].time)


                            .setSwipeLeftLabelColor(Color.WHITE)
                            .create()
                            .decorate()

                        println("Directions: dx =$dX, dy= $dY\nisCurrentlyActive = $isCurrentlyActive")
                    }
                    else {
                        super.onChildDraw(
                            c,
                            recyclerView,
                            viewHolder,
                            dX,
                            dY,
                            actionState,
                            isCurrentlyActive
                        )

                    }
                }
            })

        mIth.attachToRecyclerView(binding!!.recyclerView)

    }


    private fun constructMessage(key:String=""):Message{
        val currentDate =
            SimpleDateFormat("dd/MM/yy", Locale.getDefault()).format(Date())
        val currentTime =
            SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        return Message(
            key,
            mAuth.uid!!,
            user!!.uid,
            binding!!.txtMsg.text.toString(),
            0,currentDate,
            currentTime
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

        val subMsg:String

        if(msg.text.length>31){
            subMsg="${msg.text.substring(0,30)}..."
        }
        else{
            subMsg=msg.text
        }
        lastMsgRef.setValue("${subMsg}\n\nLast Seen: ${msg.date}")
        lastMsgRef2.setValue("${subMsg}\n\nLast Seen: ${msg.date}")

    }


    override fun onBackPressed() {
        //super.onBackPressed()
        binding!!.backBtn.callOnClick()
    }

}



