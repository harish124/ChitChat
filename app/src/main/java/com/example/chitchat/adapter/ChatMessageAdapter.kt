package com.example.chitchat.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.chitchat.R
import com.example.chitchat.databinding.ChatFromRowBinding
import com.example.chitchat.databinding.ChatToRowBinding
import com.example.chitchat.model.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import print.Print

class ChatMessageAdapter(val products:ArrayList<Message>, var toUserImgUrl:String=""): RecyclerView.Adapter<ChatMessageAdapter.MyViewHolder>() {
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
        val uid=FirebaseAuth.getInstance().uid


        if(msg.fromId==uid){
            val binding=holder.binding as ChatFromRowBinding
            binding.txt.text=msg.text

            Glide.with(ctx!!)
                .load(R.drawable.androidicon ?: "")
                .centerCrop()
                .into(binding.fromImg)

        }
        else{
            val binding=holder.binding as ChatToRowBinding
            binding.txt.text=msg.text
            Glide.with(ctx!!)
                .load(toUserImgUrl ?: "")
                .centerCrop()
                .into(binding.toImg)
        }



    }


    private fun hasHeSeenMine(toUserUid: String) {
        val seenRef=database.getReference("Seen/${FirebaseAuth.getInstance().uid}/${toUserUid}/hasSeen")
        seenRef.addValueEventListener(object :ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(p0: DataSnapshot) {
                println("Adapter has seen: $p0")
                if(p0.value =="1"){
                    println("True Worked")
                }
            }

        })

    }

    private fun setSeen(toUserUid:String) {
        val seenRef=database.getReference("Seen/${FirebaseAuth.getInstance().uid}/${toUserUid}")
        seenRef.child("hasSeen")
            .setValue(1)
            .addOnSuccessListener {
                println("Seen set successfully")
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