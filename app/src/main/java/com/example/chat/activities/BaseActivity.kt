package com.example.chat.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.chat.utilites.Constants
import com.example.chat.utilites.PreferenceManager
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore

open class BaseActivity: AppCompatActivity() {
    private lateinit var documentReference: DocumentReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val preferenceManager = PreferenceManager(applicationContext)
        val database: FirebaseFirestore = FirebaseFirestore.getInstance()
             documentReference = database.collection(Constants.KEY_COLLECTION_USERS)
            .document(preferenceManager.getString(Constants.KEY_USER_ID).toString())
    }
    override fun onPause(){
        super.onPause()
        documentReference.update(Constants.KEY_AVAILABILITY, 0)
    }
    override fun onResume(){
        super.onResume()
        documentReference.update(Constants.KEY_AVAILABILITY, 1)
    }
}