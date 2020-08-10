package com.example.chitchat.harish_activities.ui.message_acts

import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.animation.OvershootInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.chitchat.R
import com.example.chitchat.databinding.ActivityChatMessageBinding
import com.example.chitchat.harish_activities.adapter.ChatMessageAdapter
import com.example.chitchat.harish_activities.model.User
import com.example.chitchat.harish_activities.notification.*
import com.example.chitchat.harish_activities.ui.FirstScreen
import com.example.chitchat.harish_activities.view_model.ChatMsgVM
import com.example.chitchat.harish_activities.model.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.iid.FirebaseInstanceId
import frame_transition.Transition
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator
import libs.mjn.prettydialog.PrettyDialog
import libs.mjn.prettydialog.PrettyDialogCallback
import print.Print
import retrofit2.Call
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*


class ChatMessageActivity : AppCompatActivity() {

    private var binding: ActivityChatMessageBinding?=null
    private val mAuth = FirebaseAuth.getInstance()
    private val database= FirebaseDatabase.getInstance()
    private var apiService:APIService?=null
    private val transition=Transition(this)
    private val messages= arrayListOf<Message>()
    private val mAdapter= ChatMessageAdapter(messages)
    private val p= Print(this)
    private var vm:ChatMsgVM?=null
    var user: User=User()
    var me:User=User("NetworkError")
    private var pos=-1
    private var deletedMsg= Message()
    private var deviceToken=""
    private var objValEventListener:ValueEventListener?=null



    private fun fetchUser() = intent.getParcelableExtra<User>("UserObj")
    //private fun setStatus() = database.getReference("Users/${mAuth.uid}/status").setValue("Online")
    private fun configRecyclerView() {
        val lm=LinearLayoutManager(this@ChatMessageActivity)

        lm.stackFromEnd=true
        binding?.recyclerView?.apply {
            setHasFixedSize(true)
            layoutManager=lm
            adapter=mAdapter
//            adapter=ScaleInAnimationAdapter(mAdapter).apply{
//                setFirstOnly(false)
//                setDuration(1000)
//                setHasStableIds(false)
//                setInterpolator(OvershootInterpolator(.100f))
//            }
            itemAnimator= SlideInUpAnimator(OvershootInterpolator(1f))
        }
    }
    private fun fetchMe(){
        database.getReference("Users/${mAuth.uid}")
            .addListenerForSingleValueEvent(object :ValueEventListener{
                override fun onCancelled(p0: DatabaseError) {
                    println("Can't fetch me")
                }
                override fun onDataChange(p0: DataSnapshot) {
                    me=p0.getValue(User::class.java)!!
                    println("Me: $me\np0: $p0")
                }
            })
    }

    private fun setStatus(status:String="Offline")= database.getReference("Users/${mAuth.uid}/status").setValue(status)

    private fun init(){
        try {
            user=fetchUser()!!
        } catch (e: Exception) {
            println("First fetch failed\n${e.message}")
            val intent=Intent(this,FirstScreen::class.java)
            startActivity(intent)
            this.finish()
        }
        fetchMe()
        fetchDeviceToken()
        setStatus()
        vm=ViewModelProvider(this).get(ChatMsgVM::class.java)
        mAdapter.chatMsgActBinding=binding
        binding!!.userName.text=user.uname
        Glide.with(this)
            .load(user.profile)
            .centerCrop()
            .into(binding!!.profileImage)
        mAdapter.toUserImgUrl=user.profile
        mAdapter.toUserUid=user.uid

        binding!!.vm=vm
        configRecyclerView()
        setItemTouchHelper()
        listenOnlineStatus()
        setObservers()
        apiService= Client().getClient("https://fcm.googleapis.com/")?.create(APIService::class.java)
        observeUserTyping()

        addOnClickListeners()
    }

    private fun addOnClickListeners() {
        binding!!.sendBtn.setOnClickListener{
            onSendBtnPressed()
        }

        binding!!.backBtn.setOnClickListener{
            transition.goTo(FirstScreen::class.java)
            finish()
        }

        binding!!.repTxt.setOnClickListener {
            binding!!.repTxt.visibility=View.GONE
        }
    }

    private fun listenOnlineStatus() {
        database.getReference("Users/${user.uid}/status")
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
        val ref=database.getReference("last_messages/${user.uid}/${mAuth.uid}/isTyping")

        ref.addValueEventListener(object:ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    if(p0.value.toString()=="1"){
                        binding!!.userName.text="${user.uname} is Typing..."
                        binding!!.userName.setTextColor(Color.GREEN)
                    }
                    else{
                        binding!!.userName.text="${user.uname}"
                        binding!!.userName.setTextColor(Color.WHITE)
                    }
                }
            }

        })
    }

    private fun setObservers() {
        vm!!.txtMsg.observe(this, androidx.lifecycle.Observer{msg->
            val ref=database.getReference("last_messages/${mAuth.uid}/${user.uid}/isTyping")
            if (msg.isBlank()) {
                ref.setValue(0)
            }
            else{
                ref.setValue(1)
            }
        })
    }

    private fun fetchDeviceToken() {
        FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener {
            deviceToken=it.token
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=DataBindingUtil.setContentView(this,R.layout.activity_chat_message)

        init()
        listenForMessages(user.uid)

    }

    override fun onPause() {
        try {
            database.getReference("messages")
                .removeEventListener(objValEventListener!!)

        }
        catch (e:java.lang.Exception){
            println("\nNot a big error\n${e.message}")
        }
        finally {
            super.onPause()
        }

        setStatus("0")
    }

    override fun onResume() {
        super.onResume()
        try {
            database.getReference("messages")
                .addValueEventListener(objValEventListener!!)
        } catch (e: Exception) {
            println("Error: ${e.message}")
        }
        setStatus("Online")
    }

    private fun onSendBtnPressed() {
        sendToFirebase()

        binding!!.txtMsg.setText("")

        val seenRef=database.getReference("last_messages/${user.uid}/${mAuth.uid}/hasSeen")
        seenRef.setValue(0)
            .addOnSuccessListener {
                println("Seen set successfully")
            }
    }

    private fun listenForMessages(toUserUid:String) {
        objValEventListener=object :ValueEventListener{
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
                binding!!.recyclerView.smoothScrollToPosition(mAdapter.itemCount)
            }

        }

        val objChildEventListener=object:ChildEventListener{
            override fun onCancelled(p0: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {
                TODO("Not yet implemented")
            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                println("""
                    onChildChanged:
                    ${p0.value}
                    ${messages.contains(p0.getValue(Message::class.java))}
                """.trimIndent())
                mAdapter.notifyDataSetChanged()
            }

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                if(p0.exists()){
                    val msg=p0.getValue(Message::class.java) ?: Message(text="Error in Retrieving Data")
                    if((msg.toId==toUserUid) and (msg.fromId==mAuth.uid)) {
                        println("From me: $msg")
                        messages.add(msg)
                        mAdapter.notifyItemInserted(mAdapter.itemCount+1)
                    }
                    else if((msg.fromId==toUserUid) and (msg.toId==mAuth.uid)) {
                        println("From him: $msg")
                        messages.add(msg)
                        mAdapter.notifyItemInserted(mAdapter.itemCount+1)
                    }


                }
            }

            override fun onChildRemoved(p0: DataSnapshot) {
                println("""
                    OnChildRemoved:
                    ${p0.value}
                    
                """.trimIndent())
            }

        }
        database.getReference("messages")
            //.addValueEventListener(objValEventListener!!)
            .addChildEventListener(objChildEventListener)
    }

    private fun setItemTouchHelper() {
        val mIth = ItemTouchHelper(
            object : ItemTouchHelper.SimpleCallback(
                0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
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
                        if(direction==ItemTouchHelper.RIGHT){
                            var flag=false
                            deletedMsg=messages[pos]
                            val pd=PrettyDialog(this@ChatMessageActivity)
                            if (deletedMsg.fromId==mAuth.uid) {
                                if(deletedMsg.delete==1){
                                    return
                                }
                                pd.setTitle("Delete")
                                    .setIcon(R.drawable.pdlg_icon_info)
                                    .setMessage("Do you want to delete this msg?\n\"${deletedMsg.text}\"")
                                    .addButton(
                                        "Delete for me",					// button text
                                        R.color.pdlg_color_white,		// button text color
                                        R.color.pdlg_color_green,		// button background color
                                        PrettyDialogCallback {
                                            //p.sprintf("Deleted")
                                            println("Deleted")
                                            messages.removeAt(pos)
                                            mAdapter.notifyItemRemoved(pos)
                                            pd.dismiss()
                                            flag=true
                                            if(deletedMsg.delete==2){
                                                deleteMessageFromFb(deletedMsg,3)
                                            }
                                            else{
                                                deleteMessageFromFb(deletedMsg,1)
                                            }
                                        }
                                    )
                                    .addButton(
                                        "Delete for everyone",					// button text
                                        R.color.pdlg_color_white,		// button text color
                                        R.color.pdlg_color_green,		// button background color
                                        PrettyDialogCallback {
                                            //p.sprintf("Deleted")
                                            println("Deleted")
                                            messages.removeAt(pos)
                                            mAdapter.notifyItemRemoved(pos)
                                            pd.dismiss()
                                            flag=true
                                            deleteMessageFromFb(deletedMsg,3)
                                        }
                                    )
                                    .addButton(
                                        "Cancel",					// button text
                                        R.color.pdlg_color_white,		// button text color
                                        R.color.pdlg_color_green,		// button background color
                                        PrettyDialogCallback {
                                            //p.sprintf("Cancelled")
                                            println("Cancelled")
                                            pd.dismiss()
                                        }
                                    )
                                pd.setCancelable(false)
                                pd.setCanceledOnTouchOutside(false)
                                pd.show()


                            }
                            else {
                                if(deletedMsg.delete==2){
                                    return
                                }
                                pd.setTitle("Delete for me")
                                    .setIcon(R.drawable.pdlg_icon_info)
                                    .setMessage("Do you want to delete this msg?\n\"${deletedMsg.text}\"")
                                    .addButton(
                                        "OK",					// button text
                                        R.color.pdlg_color_white,		// button text color
                                        R.color.pdlg_color_green,		// button background color
                                        PrettyDialogCallback {
                                            //p.sprintf("Deleted")
                                            println("Deleted")
                                            messages.removeAt(pos)
                                            mAdapter.notifyItemRemoved(pos)
                                            pd.dismiss()
                                            flag=true

                                            if(deletedMsg.delete==1){
                                                deleteMessageFromFb(deletedMsg,3)
                                            }
                                            else{
                                                deleteMessageFromFb(deletedMsg,2)
                                            }
                                        }
                                    )
                                    .addButton(
                                        "Cancel",					// button text
                                        R.color.pdlg_color_white,		// button text color
                                        R.color.pdlg_color_green,		// button background color
                                        PrettyDialogCallback {
                                            //p.sprintf("Cancelled")
                                            println("Cancelled")
                                            pd.dismiss()
                                        }
                                    )
                                pd.setCancelable(false)
                                pd.setCanceledOnTouchOutside(false)
                                pd.show()
                            }
                        }
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

                        if(dX>0){   //swipe right

                            if (deletedMsg.fromId==mAuth.uid) {
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
                                    .addSwipeRightBackgroundColor(R.color.grey)
                                    .create()
                                    .decorate()

                                println("Directions: dx =$dX, dy= $dY\nisCurrentlyActive = $isCurrentlyActive")
                            }

                        }
                        if (dX<0) {//only when swiping left side
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

    private fun deleteMessageFromFb(msg:Message,whom:Int) {
        if (whom==3) {
            database.getReference("messages/${msg.msgId}")
                .removeValue()
                .addOnSuccessListener {
                    println("Message Deleted from FB successfully")
                }
        } else {
            database.getReference("messages/${msg.msgId}/delete")
                .setValue(whom)
                .addOnSuccessListener {
                    println("Message Deleted locally successfully")
                }
        }
    }

    private fun constructMessage(key:String=""): Message {
        val currentDate =
            SimpleDateFormat("dd/MM/yy", Locale.getDefault()).format(Date())
        val currentTime =
            SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

        var repMsg=""
        var replyPos=-1
        if(binding!!.repTxt.visibility==View.VISIBLE){
            println("Reply==1 in chatmsgact")
            repMsg=binding!!.repTxt.text.toString()
            binding!!.repTxt.visibility=View.GONE
            replyPos=Integer.parseInt(binding!!.replyPos.text.toString())
        }


        return Message(
            key,
            mAuth.uid!!,
            user.uid,
            binding!!.txtMsg.text.toString(),
            0,currentDate,
            currentTime,
            0,
            repMsg,
            replyPos
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
        if(binding!!.onlineBall.visibility==View.INVISIBLE){
            sendNotification(me.uname,msg.text,user.deviceToken)
        }
    }

    private fun sendNotification(title: String, msg: String, toDeviceToken: String) {
        val data= Data(title=title,body=msg,sender = me)
        val notificationSender=NotificationSender(data,toDeviceToken)

        apiService?.sendNotification(notificationSender)?.enqueue(object : retrofit2.Callback<MyResponse> {
            override fun onFailure(call: Call<MyResponse>, t: Throwable) {
                println("Not able to send notification\n${t.message}")
            }

            override fun onResponse(call: Call<MyResponse>, response: Response<MyResponse>) {
                if(response.code()==200){
                    if(response.body()?.success!=1){
                        println("Not able to send notification")
                    }
                }
            }

        })
    }

    private fun setLastMsg(msg: Message){
        val lastMsgRef=database.getReference("last_messages/${mAuth.uid}/${user.uid}/lastMsg")
        val lastMsgRef2=database.getReference("last_messages/${user.uid}/${mAuth.uid}/lastMsg")

        val subMsg:String = if(msg.text.length>31){
            "${msg.text.substring(0,30)}..."
        } else{
            msg.text
        }
        lastMsgRef.setValue("${subMsg}\n\nLast Seen: ${msg.date}")
        lastMsgRef2.setValue("${subMsg}\n\nLast Seen: ${msg.date}")

    }

    override fun onBackPressed() {
        //super.onBackPressed()
        binding!!.backBtn.callOnClick()
    }

}



