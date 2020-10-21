

package com.example.chatapp


import android.app.Activity
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_register.*
import timber.log.Timber
import java.util.*

class RegisterActivity : AppCompatActivity() {
    private lateinit var auth:FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth=FirebaseAuth.getInstance()
        register_button.setOnClickListener {
            val email=email_edittext_register.text.toString()
            val password=password_edittext_register.text.toString()
            auth.createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener(this){task->
                    if(task.isSuccessful){
                        Timber.d("Created User Successfully")
                        val user=auth.currentUser
                        
                        uploadImageToFirebase()
                    }else{
                        Timber.w(task.exception,"Failed to create User")
                        Toast.makeText(baseContext, "Authentication Failed", Toast.LENGTH_SHORT).show()
                    }

                }
        }

        selectphoto_button.setOnClickListener {
            Timber.d("trying to show photo selector")
            val intent=Intent(Intent.ACTION_PICK)
            intent.type="image/*"
            startActivityForResult(intent,0)
        }
    }

    private fun uploadImageToFirebase() {
        if(selectedPhotoUri==null) return

        val filename=UUID.randomUUID().toString()
        val ref=FirebaseStorage.getInstance().getReference("/images/$filename")
        ref.putFile(selectedPhotoUri!!)
            .addOnSuccessListener {
                Timber.d("Succesfully Uploaded image ${it.metadata?.path}")
                ref.downloadUrl.addOnSuccessListener {
                    it.toString()
                    Timber.d("File Location:$it")
                    saveUserToFirebaseDatabase(it.toString())
                }
            }
    }

    private fun saveUserToFirebaseDatabase(profileImageUrl: String) {
        val uid=FirebaseAuth.getInstance().uid ?:""
        val ref=FirebaseDatabase.getInstance().getReference("/users/$uid")
        val user=User(uid,username_edittext_register.text.toString(),profileImageUrl)
        ref.setValue(user)
            .addOnSuccessListener {
                Timber.d("Saved to Firebase DB")
                val intent=Intent(this,LatestMessagesActivity::class.java)
                intent.flags=Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK) //Clearing Activities in the BackStack
                startActivity(intent)
            }
            .addOnFailureListener {
                Timber.e("Failed to enter into DB")
            }
    }

    var selectedPhotoUri: Uri?=null
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode==0&&resultCode==Activity.RESULT_OK&&data!=null){
            Timber.d("Photo was selected")
            selectedPhotoUri=data.data
            val bitmap= MediaStore.Images.Media.getBitmap(contentResolver,selectedPhotoUri)
            val bitmapDrawable=BitmapDrawable(bitmap)
            selectphoto_button.setBackgroundDrawable(bitmapDrawable)
        }
    }
}
class User(val uid:String,val username:String,val profileImageUrl:String)