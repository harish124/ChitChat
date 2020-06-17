//package com.example.chitchat.harish_activities.ui
//
//import android.os.Bundle
//import androidx.fragment.app.Fragment
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.view.animation.OvershootInterpolator
//import android.widget.SearchView
//import androidx.databinding.DataBindingUtil
//import androidx.recyclerview.widget.RecyclerView
//import androidx.recyclerview.widget.StaggeredGridLayoutManager
//import com.example.chitchat.R
//import com.example.chitchat.databinding.FragmentChatsBinding
//import com.example.chitchat.databinding.FragmentGroupBinding
//import com.example.chitchat.harish_activities.adapter.FragmentChatsRVAdapter
//import com.example.chitchat.harish_activities.model.Group
//import com.example.chitchat.harish_activities.model.User
//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.database.DataSnapshot
//import com.google.firebase.database.DatabaseError
//import com.google.firebase.database.FirebaseDatabase
//import com.google.firebase.database.ValueEventListener
//import frame_transition.Transition
//import jp.wasabeef.recyclerview.adapters.ScaleInAnimationAdapter
//import jp.wasabeef.recyclerview.animators.SlideInUpAnimator
//import print.Print
//
///**
// * A simple [Fragment] subclass as the default destination in the navigation.
// */
//class GroupsFragment : Fragment() {
//
//    private var binding: FragmentGroupBinding?=null
//
//    private val p= Print(context)
//
//    private val mAuth= FirebaseAuth.getInstance()
//    private val transition= Transition(context)
//    private val database= FirebaseDatabase.getInstance()
//    private var mGroups= arrayListOf<Group>()
//    private var adapter = FragmentChatsRVAdapter(mGroups)
//
//    private fun configRecyclerView() {
//
//        binding?.recyclerView?.setHasFixedSize(true)
//        binding?.recyclerView?.layoutManager = StaggeredGridLayoutManager(2, RecyclerView.VERTICAL)
//        //binding?.recyclerView?.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
//
//        binding?.recyclerView?.adapter = ScaleInAnimationAdapter(adapter).apply{
//            setFirstOnly(false)
//            setDuration(1000)
//            setHasStableIds(false)
//            setInterpolator(OvershootInterpolator(.100f))
//        }
//        binding?.recyclerView?.itemAnimator= SlideInUpAnimator(OvershootInterpolator(1f))
//    }
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        // Inflate the layout for this fragment
//        binding= DataBindingUtil.inflate(inflater,
//            R.layout.fragment_chats,
//            container, false)
//
//        configRecyclerView()
//        fetchUsers()
//
//        binding!!.searchBar.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
//            override fun onQueryTextSubmit(query: String?): Boolean {
//                searchForThisString(query!!)
//                return false
//            }
//
//            override fun onQueryTextChange(query: String?): Boolean {
//                searchForThisString(query!!)
//                return false
//            }
//
//        })
//
//        return binding!!.root
//    }
//
//    fun searchForThisString(str:String){
//        database.getReference("Users")
//            .addValueEventListener(object : ValueEventListener {
//                override fun onCancelled(p0: DatabaseError) {
//                    p.fprintf("Error in fetching Users:\n ${p0.message}")
//                }
//
//                override fun onDataChange(groups: DataSnapshot) {
//                    if(groups.exists()){
//                        mGroups.clear()
//                        adapter.notifyDataSetChanged()
//                        var i=0
//
//                        groups.children.forEach {
//                            val group=it.getValue(Group::class.java) ?: Group(gname="RetrievalError")
//                            mGroups.add(group)
//                            adapter.notifyItemInserted(i)
//                            i+=1
//                        }
//                    }
//                }
//
//            })
//    }
//
//    private fun fetchUsers() {
//        database.getReference("Groups")
//            .addValueEventListener(object : ValueEventListener {
//                override fun onCancelled(p0: DatabaseError) {
//                    println("Error in fetching Groups:\n ${p0.message}")
//                }
//
//                override fun onDataChange(groups: DataSnapshot) {
//                    if(groups.exists()){
//                        mGroups.clear()
//                        adapter.notifyDataSetChanged()
//                        var i=0
//
//                        groups.children.forEach {
//                            val group=it.getValue(Group::class.java) ?: Group(gname="RetrievalError")
//                            mGroups.add(group)
//                            adapter.notifyItemInserted(i)
//                            i+=1
//                        }
//                    }
//                }
//
//            })
//    }
//}
