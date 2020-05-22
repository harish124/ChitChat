package com.example.chitchat.ui

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.example.chitchat.LoginSignUp
import com.example.chitchat.R
import com.example.chitchat.adapter.FragmentAdapter
import com.example.chitchat.databinding.ActivityFirstScreenBinding
import com.example.chitchat.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import frame_transition.Transition
import kotlinx.android.synthetic.main.activity_first_screen.*
import print.Print

class FirstScreen : AppCompatActivity() {

    private var binding:ActivityFirstScreenBinding?=null

    private val p= Print(this)

    private val mAuth = FirebaseAuth.getInstance()
    private val transition= Transition(this)
    private val database= FirebaseDatabase.getInstance()

    private fun init(){
        binding!!.viewPager.adapter=FragmentAdapter(supportFragmentManager)
        binding!!.tabLayout.setupWithViewPager(viewPager)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=DataBindingUtil.setContentView(this,
            R.layout.activity_first_screen
        )
        setSupportActionBar(toolbar_main)

        supportActionBar!!.title="ChitChat"
        init()

        fetchDisplayNameAndProfile()

    }

    private fun fetchDisplayNameAndProfile() {
        database.getReference("Users")
            .child(mAuth.currentUser!!.uid)
            .addValueEventListener(object : ValueEventListener{
                override fun onCancelled(p0: DatabaseError) {
                    TODO("Not yet implemented")
                }

                override fun onDataChange(userDetails: DataSnapshot) {
                    if(userDetails.exists()) {
                        val user = userDetails.getValue(User::class.java)
                        try {
                            Glide.with(this@FirstScreen)
                                .load(user?.profile)
                                .centerCrop()
                                .into(binding!!.imgView)
                        } catch (e: Exception) {
                            e.printStackTrace()
                            println("Not a big deal")
                        }
                    }
                }

            })
    }

    private fun setStatus(status:String="Offline"){
        val ref=database.getReference("Users/${mAuth.uid}/status")
        ref.setValue(status)
    }

    override fun onResume() {
        super.onResume()
        setStatus("Online")
    }

    override fun onPause() {
        super.onPause()
        setStatus("Offline")
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.chit_chat_main_menu,menu)
        return super.onCreateOptionsMenu(menu)
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.menuLogOut->{
                mAuth.signOut()
                p.sprintf("Logged Out Successfully")
                transition.goTo(LoginSignUp::class.java)
            }
        }
        return super.onOptionsItemSelected(item)
    }

}
