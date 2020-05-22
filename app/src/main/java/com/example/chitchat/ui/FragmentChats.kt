package com.example.chitchat.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import android.widget.SearchView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.chitchat.adapter.FragmentChatsRVAdapter
import com.example.chitchat.R
import com.example.chitchat.databinding.FragmentChatsBinding
import com.example.chitchat.model.User
import com.example.chitchat.ui.message_acts.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import frame_transition.Transition
import jp.wasabeef.recyclerview.adapters.ScaleInAnimationAdapter
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator
import print.Print


class FragmentChats : Fragment() {

    private var binding:FragmentChatsBinding?=null

    private val p=Print(context)

    private val mAuth=FirebaseAuth.getInstance()
    private val transition= Transition(context)
    private val database= FirebaseDatabase.getInstance()
    private var mUsers= arrayListOf<User>()
    private var adapter = FragmentChatsRVAdapter(mUsers)


    private fun configRecyclerView() {

        binding?.recyclerView?.setHasFixedSize(true)
        binding?.recyclerView?.layoutManager = StaggeredGridLayoutManager(2,RecyclerView.VERTICAL)
        //binding?.recyclerView?.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))

        binding?.recyclerView?.adapter = ScaleInAnimationAdapter(adapter).apply{
            setFirstOnly(false)
            setDuration(1000)
            setHasStableIds(false)
            setInterpolator(OvershootInterpolator(.100f))
        }
        binding?.recyclerView?.itemAnimator= SlideInUpAnimator(OvershootInterpolator(1f))
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding= DataBindingUtil.inflate(inflater,
            R.layout.fragment_chats,
            container, false)

        configRecyclerView()
        fetchUsers()


        binding!!.searchBar.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                searchForThisString(query!!)
                return false
            }

            override fun onQueryTextChange(query: String?): Boolean {
                searchForThisString(query!!)
                return false
            }

        })

        return binding!!.root
    }

    fun searchForThisString(str:String){
        database.getReference("Users")
            .addValueEventListener(object :ValueEventListener{
                override fun onCancelled(p0: DatabaseError) {
                    p.fprintf("Error in fetching Users:\n ${p0.message}")
                }

                override fun onDataChange(usersList: DataSnapshot) {
                    if(usersList.exists()){
                        mUsers.clear()
                        adapter.notifyDataSetChanged()
                        var i=0
                        for(users in usersList.children){
                            val user=users?.getValue(User::class.java) ?:User("SomethingWentWrong")
                            if(user.uid!=mAuth.currentUser?.uid){
                                if(user.uname.toLowerCase().contains(str)) {
                                    //p?.sprintf("Uname = ${user.uname}")
                                    mUsers.add(user)
                                    adapter.notifyItemInserted(i)
                                    i += 1
                                }
                            }
                        }
                    }
                }

            })
    }

    private fun fetchUsers() {
        database.getReference("Users")
            .addValueEventListener(object :ValueEventListener{
                override fun onCancelled(p0: DatabaseError) {
                    p.fprintf("Error in fetching Users:\n ${p0.message}")
                }

                override fun onDataChange(usersList: DataSnapshot) {
                    if(usersList.exists()){
                        mUsers.clear()
                        adapter.notifyDataSetChanged()
                        var i=0
                        for(users in usersList.children){
                            val user:User=users?.getValue(User::class.java)?:User("SomethingWentWrong")



                            if(user.uid!=mAuth.currentUser?.uid){
                                //p?.sprintf("Uname = ${user.uname}")
                                mUsers.add(user)
                                adapter.notifyItemInserted(i)
                                i+=1
                            }
                        }
                    }
                }

            })
    }


    override fun onResume() {
        super.onResume()
        fetchUsers()
    }

}
