package com.example.chitchat.harish_activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.databinding.DataBindingUtil
import com.example.chitchat.R
import com.example.chitchat.databinding.ActivityMainBinding
import com.example.chitchat.harish_activities.model.User
import com.example.chitchat.harish_activities.ui.FirstScreen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.iid.FirebaseInstanceId
import frame_transition.Transition
import print.Print


class LoginSignUp : Activity() {

    private var binding:ActivityMainBinding?=null
    private var email=""
    private var pwd=""

    private var state=false
    private val p=Print(this)
    private val mAuth = FirebaseAuth.getInstance()
    private val transition= Transition(this)
    private val database= FirebaseDatabase.getInstance()
    private var deviceToken:String=""

    private var uid=mAuth.currentUser?.uid.toString()


    private fun checkAlreadySignedIn(){
        if(mAuth.currentUser!=null){
            transition.goTo(FirstScreen::class.java)
            //p.sprintf("Welcome Back ${mAuth.currentUser}")
        }
    }
    private fun fetchDeviceToken() {
        FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener {
            deviceToken=it.token
            println("Device Token: $deviceToken")

        }
    }
    private fun addOnClickListeners() {
        binding!!.signInBtn.setOnClickListener{
            signIn()
        }

        binding!!.loginBtn.setOnClickListener{
            logIn()
        }

        binding!!.createAccountLabel.setOnClickListener{
            onCreateAccountLabelClicked()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=DataBindingUtil.setContentView(this,
            R.layout.activity_main
        )

        checkAlreadySignedIn()
        fetchDeviceToken()
        addOnClickListeners()

    }

    private fun signIn(){
        email=binding!!.uname.text.toString()
        pwd=binding!!.pwd.text.toString()
        val name=binding!!.name.text.toString()
        mAuth.createUserWithEmailAndPassword(email, pwd)
            .addOnCompleteListener(
                this
            ) { task ->
                if (task.isSuccessful) {
                    Log.d("MainActivity", "createUserWithEmail:success")
                    p.sprintf("Sign Up Successful")

                    uid=FirebaseAuth.getInstance().uid.toString()
                    val user= User(name,"",""
                        ,uid, "offline",deviceToken)

                    database.reference
                        .child("Users")
                        .child(FirebaseAuth.getInstance().uid.toString())
                        .setValue(user)
                        .addOnCompleteListener{
                            if(it.isSuccessful){
                                p.sprintf("User data added")
                                updateToken()
                            }
                            else{
                                p.sprintf("User data was not added\nError: ${it.exception?.message}")
                            }
                        }
                    transition.goTo(FirstScreen::class.java)
                } else {
                    Log.d(
                        "MainActivity",
                        "createUserWithEmail:failure",
                        task.exception
                    )
                    p.fprintf("Authentication Failed\nError ${task.exception?.message}")

                }
            }
    }
    private fun logIn(){
        email=binding!!.uname.text.toString()
        pwd=binding!!.pwd.text.toString()
        mAuth.signInWithEmailAndPassword(email,pwd)
            .addOnCompleteListener{task->
                if(task.isSuccessful){
                    p.sprintf("Sign In Successful")
                    updateToken()
                    val intent= Intent(this,FirstScreen::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    startActivity(intent)

                } else {
                    p.fprintf("Sign In Failed\nError ${task.exception?.message}")

                }
            }

    }

    private fun updateToken() {
        database.getReference("Users/${mAuth.uid}/deviceToken")
            .setValue(deviceToken)
        println("Token Updated")
    }

    private fun onCreateAccountLabelClicked(){
        if(!state){
            state=true
            binding!!.signInBtn.visibility= View.VISIBLE
            binding!!.name.visibility=View.VISIBLE
            binding!!.createAccountLabel.text="Already have an account?"
            binding!!.loginBtn.visibility= View.INVISIBLE
        }
        else{
            state=false
            binding!!.signInBtn.visibility= View.GONE
            binding!!.name.visibility=View.GONE
            binding!!.loginBtn.visibility= View.VISIBLE
            binding!!.createAccountLabel.text="New Users Click Here!"
        }
    }
}
