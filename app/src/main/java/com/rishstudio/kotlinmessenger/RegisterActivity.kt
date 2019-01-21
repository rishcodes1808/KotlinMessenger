package com.rishstudio.kotlinmessenger

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        already_have_account_textview.setOnClickListener {
            val loginIntent = Intent(this, LoginActivity::class.java)
            startActivity(loginIntent)
        }

        register_button_register.setOnClickListener {
            performRegister()
        }

        select_photo_register.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent,0)
        }
    }

    var selectedPhotoUri : Uri? = null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == 0 && resultCode == Activity.RESULT_OK && data != null){
            selectedPhotoUri = data.data
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver,selectedPhotoUri)
            select_photo_imageview_register.setImageBitmap(bitmap)
            select_photo_register.alpha = 0f
//            val bitmapDrawable =  BitmapDrawable(bitmap)
//            select_photo_register.setBackgroundDrawable(bitmapDrawable)
        }
    }

    private fun performRegister() {
        val email = email_edittext_register.text.toString()
        val password = password_edittext_register.text.toString()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please Enter Username and password", Toast.LENGTH_LONG).show()
        }

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password).addOnCompleteListener {
            if (!it.isSuccessful) return@addOnCompleteListener

            Log.d("Main", "Successfully Created User with uid ${it.result?.user?.uid}")

            uploadImageToFirebaseStorage()

        }.addOnFailureListener {
            Log.d("Main", "Failed to create user ${it.message}")
            Toast.makeText(this, "Failed to create user ${it.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun uploadImageToFirebaseStorage() {
        if (selectedPhotoUri == null) return
        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")

        ref.putFile(selectedPhotoUri!!).addOnSuccessListener {
            Log.d("Register","Successfully uploaded image ${it.metadata?.path}")
            ref.downloadUrl.addOnSuccessListener {

                Log.d("RegisterActivity","File Location $it")
                saveUserToFirebaseDatabase(it.toString())
            }
        }.addOnFailureListener{
            // TODO:-
        }

    }

    private fun saveUserToFirebaseDatabase(profileImageUrl: String) {
        val uid = FirebaseAuth.getInstance().uid ?: ""
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")

        val user = User(uid,username_edittext_register.text.toString(),profileImageUrl)
        ref.setValue(user).addOnSuccessListener {
            Log.d("RegisterActivity","Saved the user to Firebase Database")
        }
    }
}

class User(val uid: String,val username : String, val profileImageUrl: String)
