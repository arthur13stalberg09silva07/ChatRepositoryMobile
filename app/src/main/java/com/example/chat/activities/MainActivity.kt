package com.example.chat.activities

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.Toast
import com.example.chat.adapters.RecentConversationsAdapter
import com.example.chat.databinding.ActivityMainBinding
import com.example.chat.listeners.ConversionListener
import com.example.chat.models.ChatMessage
import com.example.chat.models.User
import com.example.chat.utilites.Constants
import com.example.chat.utilites.PreferenceManager
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.messaging.FirebaseMessaging


class MainActivity : BaseActivity(), ConversionListener {

    private lateinit var bindind: ActivityMainBinding
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var conversations: MutableList<ChatMessage>
    private lateinit var conversationsAdapter: RecentConversationsAdapter
    private lateinit var database: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindind = ActivityMainBinding.inflate(layoutInflater)
        setContentView(bindind.root)
        preferenceManager = PreferenceManager(applicationContext)

        init()
        loadUserDetails()
        getToken()
        setListeners()
        listenConversations()
    }

    private fun init() {
        conversations = ArrayList()
        conversationsAdapter = RecentConversationsAdapter(conversations, this)
        bindind.conversationsRecyclerView.adapter = conversationsAdapter
        database = FirebaseFirestore.getInstance()
    }


    private fun setListeners(){
        bindind.imageSignOut.setOnClickListener { signOut() }
        bindind.fabNewChat.setOnClickListener { startActivity(Intent(applicationContext, UsersActivity::class.java))}
    }

    private fun loadUserDetails(){
        bindind.textName.text = preferenceManager.getString(Constants.KEY_NAME)
        val bytes = Base64.decode(preferenceManager.getString(Constants.KEY_IMAGE), Base64.DEFAULT)
        val bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.size)
        bindind.imageProfile.setImageBitmap(bitmap)
    }

    private fun showToast(text: String){
        Toast.makeText(this, text, Toast.LENGTH_LONG).show()
    }

    private fun listenConversations() {
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
            .whereEqualTo(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
            .addSnapshotListener(eventListener)
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
            .whereEqualTo(Constants.KEY_RECEIVER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
            .addSnapshotListener(eventListener)
    }

    private val eventListener = EventListener<QuerySnapshot> { value, error ->
        if (error != null) {
            return@EventListener
        }
        if (value != null) {
            for (documentChange in value.documentChanges) {
                if (documentChange.type == DocumentChange.Type.ADDED) {
                    val senderId = documentChange.document.getString(Constants.KEY_SENDER_ID) ?: ""
                    val receiverId = documentChange.document.getString(Constants.KEY_RECEIVER_ID) ?: ""
                    val chatMessage = ChatMessage()
                    chatMessage.senderId = senderId
                    chatMessage.receiverId = receiverId
                    if(preferenceManager.getString(Constants.KEY_USER_ID).equals(senderId)){
                        chatMessage.conversionImage = documentChange.document.getString(Constants.KEY_RECEIVER_IMAGE) ?: ""
                        chatMessage.conversionName = documentChange.document.getString(Constants.KEY_RECEIVER_NAME) ?: ""
                        chatMessage.conversionId = documentChange.document.getString(Constants.KEY_RECEIVER_ID) ?: ""
                    }else{
                        chatMessage.conversionImage = documentChange.document.getString(Constants.KEY_SENDER_IMAGE) ?: ""
                        chatMessage.conversionName = documentChange.document.getString(Constants.KEY_SENDER_NAME) ?: ""
                        chatMessage.conversionId = documentChange.document.getString(Constants.KEY_SENDER_ID) ?: ""
                    }
                    chatMessage.message = documentChange.document.getString(Constants.KEY_LAST_MESSAGE) ?: ""
                    chatMessage.dataObject = documentChange.document.getDate(Constants.KEY_TIMESTAMP)!!
                    conversations.add(chatMessage)
                }else if(documentChange.type == DocumentChange.Type.MODIFIED){
                    for (i in conversations.indices) {
                        val senderId = documentChange.document.getString(Constants.KEY_SENDER_ID)
                        val receiverId = documentChange.document.getString(Constants.KEY_RECEIVER_ID)
                        if (conversations[i].senderId == senderId && conversations[i].receiverId == receiverId) {
                            conversations[i].message = documentChange.document.getString(Constants.KEY_LAST_MESSAGE) ?: ""
                            conversations[i].dataObject = documentChange.document.getDate(Constants.KEY_TIMESTAMP)!!
                            break
                        }
                    }
                }
            }
            conversations.sortWith { obj1, obj2 -> obj2.dataObject!!.compareTo(obj1.dataObject!!) }
            conversationsAdapter.notifyDataSetChanged()
            bindind.conversationsRecyclerView.smoothScrollToPosition(0)
            bindind.conversationsRecyclerView.visibility = View.VISIBLE
            bindind.progressBar.visibility = View.GONE
        }
    }

    private fun getToken(){
        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { updateToken(it) }
    }

    private fun updateToken(token: String){
        preferenceManager.putString(Constants.KEY_FCM_TOKEN, token)
        val database = FirebaseFirestore.getInstance()
        val documentReference = database.collection(Constants.KEY_COLLECTION_USERS)
            .document(preferenceManager.getString(Constants.KEY_USER_ID)!!)
        documentReference.update(Constants.KEY_FCM_TOKEN, token)
            .addOnFailureListener { showToast("Unable to update token") }

    }

    private fun signOut() {
        showToast("Signing out...")
        val database = FirebaseFirestore.getInstance()
        val documentReference = preferenceManager.getString(Constants.KEY_USER_ID)?.let {
            database.collection(Constants.KEY_COLLECTION_USERS).document(it)
        }
        val updates = hashMapOf<String, Any>( Constants.KEY_FCM_TOKEN to FieldValue.delete() )
        documentReference?.update(updates)
            ?.addOnSuccessListener {
                preferenceManager.clear()
                startActivity(Intent(applicationContext,SignInActivity::class.java))
                finish()
            }
            ?.addOnFailureListener { showToast("Unable to sign out") }

    }

    override fun onConversionClicked(user: User) {
        val intent = Intent(applicationContext, ChatActivity::class.java)
        intent.putExtra(Constants.KEY_USER, user)
        startActivity(intent)
    }
}