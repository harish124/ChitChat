package com.example.chitchat

import android.app.Activity
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.chitchat.databinding.ActivityMainBinding
import com.example.chitchat.model.User
import com.example.chitchat.ui.FirstScreen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import frame_transition.Transition
import print.Print


class LoginSignUp : Activity() {

    private var binding:ActivityMainBinding?=null
    private var email=""
    private var pwd=""


    private val p=Print(this)
    private val mAuth = FirebaseAuth.getInstance()
    private val transition= Transition(this)
    private val database= FirebaseDatabase.getInstance()
    private val uid=mAuth.currentUser?.uid.toString() ?: "404"




    private fun checkAlreadySignedIn(){
        if(mAuth.currentUser!=null){
            transition.goTo(FirstScreen::class.java)
            p.sprintf("Welcome Back ${mAuth.currentUser}")
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=DataBindingUtil.setContentView(this,
        R.layout.activity_main)


        checkAlreadySignedIn()

        binding!!.signInBtn.setOnClickListener{
            email=binding!!.uname.text.toString()
            pwd=binding!!.pwd.text.toString()
            mAuth.createUserWithEmailAndPassword(email, pwd)
                .addOnCompleteListener(
                    this
                ) { task ->
                    if (task.isSuccessful) {
                        Log.d("MainActivity", "createUserWithEmail:success")
                        p.sprintf("Sign Up Successfull")



                        val user= User("Harish","",""
                            ,uid, "offline")

                        database.reference
                            .child("Users")
                            .child(uid)
                            .setValue(user)
                            .addOnCompleteListener{
                                if(it.isSuccessful){
                                    p.sprintf("User data added")
                                }
                                else{
                                    p.sprintf("User data was not added\nError: ${it.exception?.message}")
                                }
                            }
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

        binding!!.loginBtn.setOnClickListener{
            email=binding!!.uname.text.toString()
            pwd=binding!!.pwd.text.toString()
            mAuth.signInWithEmailAndPassword(email,pwd)
                .addOnCompleteListener{task->
                    if(task.isSuccessful){
                        p.sprintf("Sign In Successfull")
                        transition.goTo(FirstScreen::class.java)
                    } else {
                        p.fprintf("Sign In Failed\nError ${task.exception?.message}")

                    }
                }
        }


    }
}
