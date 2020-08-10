package com.example.chitchat.harish_activities.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.chitchat.R
import com.example.chitchat.databinding.ActivityChatMessageBinding
import com.example.chitchat.databinding.ChatFromRowBinding
import com.example.chitchat.databinding.ChatToRowBinding
import com.example.chitchat.harish_activities.model.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import print.Print

class ChatMessageAdapter(private val products:ArrayList<Message>, var toUserImgUrl:String="", var toUserUid: String="", var chatMsgActBinding: ActivityChatMessageBinding?=null): RecyclerView.Adapter<ChatMessageAdapter.MyViewHolder>() {
    var p: Print?=null
    var ctx: Context?=null
    private val database= FirebaseDatabase.getInstance()
    class MyViewHolder(val binding:Any,itemView: View): RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, pos: Int): MyViewHolder {
        if (pos==1) {
            val binding: ChatFromRowBinding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.context), R.layout.chat_from_row,
                parent, false
            )
            p = Print(parent.context)
            ctx = parent.context
            return MyViewHolder(binding, binding.root)
        }
        else {
            val binding: ChatToRowBinding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.context), R.layout.chat_to_row,
                parent, false
            )
            p = Print(parent.context)
            ctx = parent.context
            return MyViewHolder(binding, binding.root)
        }

    }

    override fun getItemCount(): Int {
        return products.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val msg=products[position]
        val productsSize=products.size
        val uid=FirebaseAuth.getInstance().uid


        println("""
                Pos = $position,
                ProductsSize = $productsSize
            """.trimIndent())


        if((msg.fromId==uid)){
            val binding=holder.binding as ChatFromRowBinding
            println("""
                Msg = $msg
            """.trimIndent())
            if ((msg.delete==0) or (msg.delete==2)) {

                setOnClickListener(binding.txt,position)
                Glide.with(ctx!!)
                    .load(R.drawable.androidicon ?: "")
                    .centerCrop()
                    .into(binding.fromImg)
                if(msg.seen==1){
                    binding.seenTxt.visibility=View.VISIBLE
                }
                else{
                    binding.seenTxt.visibility=View.INVISIBLE
                }


                if(msg.replyPos!=-1){
                    println("Reply in chatmsgadapter ${msg.replyPos}\nmsg = ${msg.text}")
                    binding.repCardInner.visibility=View.VISIBLE
                    binding.repTxt.text=msg.replyMsg
                    setReplyTxtAreaOnClickListener(binding.repCardInner,msg.replyPos)
                }
                else{
                    binding.repCardInner.visibility=View.GONE
                }
                binding.txt.text=msg.text
            } else {

                binding.txt.text="This msg is deleted"
                binding.repCardInner.visibility=View.GONE
                Glide.with(ctx!!)
                    .load(R.drawable.androidicon ?: "")
                    .centerCrop()
                    .into(binding.fromImg)

            }
        }
        else{
            val binding=holder.binding as ChatToRowBinding

            if ((msg.delete==0) or (msg.delete==1)) {
                setOnClickListener(binding.txt,position)
                Glide.with(ctx!!)
                    .load(toUserImgUrl ?: "")
                    .centerCrop()
                    .into(binding.toImg)
                toUserUid=msg.fromId
                //setSeen(toUserUid)

                if(msg.seen==0){
                    msg.seen=1
                    database.getReference("messages/${msg.msgId}/seen").setValue(1)
                        .addOnCompleteListener{
                            println("Seen set")
                        }
                }

                if(msg.replyPos!=-1){
                    println("Reply in chatmsgadapter ${msg.replyPos}\nmsg = ${msg.text}")
                    binding.repCardInner.visibility=View.VISIBLE
                    binding.repTxt.text=msg.replyMsg
                    setReplyTxtAreaOnClickListener(binding.repCardInner,msg.replyPos)
                }
                else{
                    binding.repCardInner.visibility=View.GONE
                }
                binding.txt.text=msg.text
            } else {
                Glide.with(ctx!!)
                    .load(toUserImgUrl ?: "")
                    .centerCrop()
                    .into(binding.toImg)
                toUserUid=msg.fromId
                binding.txt.text="This msg is deleted"
                binding.repCardInner.visibility=View.GONE
            }

        }
    }

    private fun setReplyTxtAreaOnClickListener(tv: CardView, pos:Int){
        tv.setOnClickListener {
            chatMsgActBinding!!.recyclerView.smoothScrollToPosition(pos)
        }

    }

    private fun setOnClickListener(tv: TextView,pos: Int,userName:String="") {
        tv.setOnClickListener {
            chatMsgActBinding!!.repTxt.visibility=View.VISIBLE

            val subMsg = if(tv.text.length>31){
                "${tv.text.substring(0,30)}..."
            } else{
                "${tv.text.toString()}..."
            }

            chatMsgActBinding!!.repTxt.text=subMsg
            chatMsgActBinding!!.replyPos.text=pos.toString()

        }
    }


    override fun getItemViewType(position: Int): Int {
        val msg=products[position]
        val uid=FirebaseAuth.getInstance().uid
        return if(msg.fromId==uid){
            1
        }
        else {
            0
        }
    }
}