package com.example.chatapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*
import timber.log.Timber

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        login_button.setOnClickListener {
            val email=email_edittext_login.text.toString()
            val password=password_edittext_login.text.toString()
            FirebaseAuth.getInstance().signInWithEmailAndPassword(email,password)
                .addOnCompleteListener(this){task->
                    if(task.isSuccessful){
                        Timber.d("Sign In Success")
                        val intent= Intent(this,LatestMessagesActivity::class.java)
                        intent.flags= Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK) //Clearing Activities in the BackStack
                        startActivity(intent)
                    }else{
                        Toast.makeText(baseContext, "Login Failed", Toast.LENGTH_SHORT).show()
                    }
                }

        }

        back_to_register_textview.setOnClickListener {
            finish()
        }
    }
}