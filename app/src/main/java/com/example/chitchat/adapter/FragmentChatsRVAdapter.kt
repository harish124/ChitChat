package com.example.chitchat.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.chitchat.R
import com.example.chitchat.databinding.CardUsersBinding
import com.example.chitchat.model.User
import com.example.chitchat.ui.message_acts.ChatMessageActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

import print.Print

class FragmentChatsRVAdapter(var products:ArrayList<User>):RecyclerView.Adapter<FragmentChatsRVAdapter.MyViewHolder>() {
    var p:Print?=null
    var ctx:Context?=null
    private val mAuth= FirebaseAuth.getInstance()
    private val database= FirebaseDatabase.getInstance()
    class MyViewHolder(val binding:CardUsersBinding):RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding:CardUsersBinding= DataBindingUtil.inflate(
                LayoutInflater.from(parent.context), R.layout.card_users,
                parent, false
        )
        p = Print(parent.context)
        ctx=parent.context

        return MyViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return products.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val user=products[position]
        holder.binding.pd= products[position]
        if(user.status=="Online"){
            holder.binding.status.visibility=View.VISIBLE
        }
        else{
            holder.binding.status.visibility=View.INVISIBLE
        }
        Glide.with(ctx!!)
            .load(user.profile)
            .centerCrop()
            .into(holder.binding.profileImage)

        fetchLastMsg(user.uid,holder.binding.lastMsg)

        holder.binding.userCard.setOnClickListener {
            val intent= Intent(ctx,ChatMessageActivity::class.java)
            intent.putExtra("UserObj",products[position])
            ctx!!.startActivity(intent)
        }
    }

    private fun fetchLastMsg(toUserUid:String,textView: TextView) {
        val lastMsg=database.getReference("last_messages/${mAuth.uid}/$toUserUid/lastMsg")
        lastMsg.addValueEventListener(object :ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(p0: DataSnapshot) {
                if(p0.exists()){
                    textView.text=p0.value.toString()
                }

            }

        })


    }
}