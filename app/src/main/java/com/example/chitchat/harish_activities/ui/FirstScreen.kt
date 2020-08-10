package com.example.chitchat.harish_activities.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.webkit.URLUtil
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.example.chitchat.harish_activities.LoginSignUp
import com.example.chitchat.R
import com.example.chitchat.adapter.FragmentAdapter
import com.example.chitchat.databinding.ActivityFirstScreenBinding
import com.example.chitchat.harish_activities.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import frame_transition.Transition
import kotlinx.android.synthetic.main.activity_first_screen.*
import print.Print


class FirstScreen : AppCompatActivity() {

    private var binding:ActivityFirstScreenBinding?=null
    private val p= Print(this)
    private val mAuth = FirebaseAuth.getInstance()
    private val transition= Transition(this)
    private val database= FirebaseDatabase.getInstance()
    private val myUid=mAuth.uid

    private fun init(){
        binding=DataBindingUtil.setContentView(this,
            R.layout.activity_first_screen
        )
        setSupportActionBar(toolbar_main)
        supportActionBar!!.title="ChitChat"
        binding!!.viewPager.adapter=FragmentAdapter(supportFragmentManager)
        binding!!.tabLayout.setupWithViewPager(viewPager)

    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        init()
        fetchDisplayNameAndProfile()

    }

    private fun fetchDisplayNameAndProfile() {
        database.getReference("Users")
            .child(myUid.toString())
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

    private fun setStatus(status:String="Offline")= database.getReference("Users/${myUid}/status").setValue(status)

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

    private fun shareMyApp(){
        var shareLink=""
        database.getReference("ShareAppLink")
            .addListenerForSingleValueEvent(object :ValueEventListener{
                override fun onCancelled(p0: DatabaseError) {
                    TODO("Not yet implemented")
                }

                override fun onDataChange(p0: DataSnapshot) {
                    shareLink=p0.value.toString()
                    val intentSms = Intent(Intent.ACTION_SEND)
                    intentSms.setType("text/plain")
                    intentSms.putExtra("sms_body", shareLink)
                    startActivity(intentSms)
                }

            })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.menuLogOut->{
                setStatus("Offline")
                mAuth.signOut()
                p.sprintf("Logged Out Successfully")
                transition.goTo(LoginSignUp::class.java)

                finish()
            }
            R.id.shareChitChat->{
                shareMyApp()
            }
            R.id.updateChitChat->{
                updateChitChat()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun updateChitChat() {
        database.getReference("ShareAppLink").addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(p0: DataSnapshot) {
                if(p0.exists()){
                    val url = p0.value.toString()
                    if(!URLUtil.isValidUrl(url)){
                        p.fprintf("Updates are not available!")
                        return
                    }
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.data = Uri.parse(url)
                    startActivity(intent)
                }
            }

        })

    }

}
