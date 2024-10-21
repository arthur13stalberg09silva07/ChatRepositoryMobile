package com.example.chat.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.example.chat.adapters.UsersAdapter
import com.example.chat.databinding.ActivityUsersBinding
import com.example.chat.listeners.UserListener
import com.example.chat.models.User
import com.example.chat.utilites.Constants
import com.example.chat.utilites.PreferenceManager
import com.google.firebase.firestore.FirebaseFirestore

class UsersActivity : BaseActivity(), UserListener {

    private lateinit var binding: ActivityUsersBinding
    private lateinit var preferenceManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUsersBinding.inflate(layoutInflater)
        setContentView(binding.root)
        preferenceManager = PreferenceManager(applicationContext)
        setListeners()
        getUsers()
    }

    private fun setListeners(){
        binding.imageBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }

    private fun getUsers(){
        loading(true)
        val database = FirebaseFirestore.getInstance()
        database.collection(Constants.KEY_COLLECTION_USERS)
            .get()
            .addOnCompleteListener {
                loading(false)
                val currentUserId = preferenceManager.getString(Constants.KEY_USER_ID)
                if (it.isSuccessful && it.result != null){
                    val users = mutableListOf<User>()
                    for(queryDocumentSnapshot in it.result){
                        if(currentUserId.equals(queryDocumentSnapshot.id)){
                            continue
                        }
                        val user = User(
                            email = queryDocumentSnapshot.getString(Constants.KEY_EMAIL)!!,
                            name = queryDocumentSnapshot.getString(Constants.KEY_NAME)!!,
                            image = queryDocumentSnapshot.getString(Constants.KEY_IMAGE)!!,
                            token = queryDocumentSnapshot.getString(Constants.KEY_FCM_TOKEN),
                            id = queryDocumentSnapshot.id
                        )
                        users.add(user)
                    }
                    if (users.size > 0){
                        val usersAdapter = UsersAdapter(users, this)
                        binding.usersRecyclerView.adapter = usersAdapter
                        binding.usersRecyclerView.visibility = View.VISIBLE
                    }else{
                        showErrorMessage()
                    }
                }else{
                    showErrorMessage()
                }
            }
    }

    private fun showErrorMessage(){
        binding.textErrorMessage.text = String.format("%s", "No users available")
        binding.textErrorMessage.visibility = View.VISIBLE
    }

    private fun loading(isLoading: Boolean){
        if(isLoading){
            binding.progressBar.visibility = View.VISIBLE
        }else{
            binding.progressBar.visibility = View.INVISIBLE
        }
    }

    override fun onUserClicked(user: User) {
        val intent = Intent(applicationContext, ChatActivity::class.java)
        preferenceManager.putString(Constants.KEY_RECEIVER_ID, user.id)
        intent.putExtra(Constants.KEY_USER, user)
        startActivity(intent)
        finish()
    }

}