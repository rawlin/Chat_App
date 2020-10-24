package com.example.chatapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.xwray.groupie.ViewHolder
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_chat_log.*
import kotlinx.android.synthetic.main.activity_new_message.*
import timber.log.Timber

class ChatLogActivity : AppCompatActivity() {

    val adapter=GroupAdapter<ViewHolder>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)
        //val username=intent.getStringExtra(NewMessageActivity.USER_KEY)
        val user=intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
        supportActionBar?.title=user?.username

        listenForMessages()

        recyclerview_chat_log.adapter=adapter

        send_button_chat_log.setOnClickListener {
            Timber.d("Attempting to Send message")
            performSendMessage()
        }
    }

    private fun listenForMessages() {
        val ref=FirebaseDatabase.getInstance().getReference("/messages")

        ref.addChildEventListener(object :ChildEventListener{
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val chatMessage=snapshot.getValue(ChatMessage::class.java)
                Timber.d(chatMessage?.text)
                if(chatMessage!=null){
                    if(chatMessage.fromID==FirebaseAuth.getInstance().uid){
                        adapter.add(ChatFromItem(chatMessage.text))
                    }else{
                        adapter.add(ChatToItem(chatMessage.text))
                    }

                }

            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                TODO("Not yet implemented")
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                TODO("Not yet implemented")
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                TODO("Not yet implemented")
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    class ChatMessage(val id:String,val text: String,val fromID:String,val toID:String,val timeStamp:Long){
        constructor():this("","","","",-1)
    }

    private fun performSendMessage() {
        val text=edittest_chat_log.text.toString()
        val fromID=FirebaseAuth.getInstance().uid
        val user=intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
        val toID=user?.uid
        val reference=FirebaseDatabase.getInstance().getReference("/messages").push() //push() creates the path
        val chatMessage=ChatMessage(reference.key!!,text,fromID!!,toID!!,System.currentTimeMillis()/1000)
        reference.setValue(chatMessage)
            .addOnSuccessListener {
                Timber.d("Saved Message ${reference.key}")
            }
    }
}
class ChatFromItem(val text:String): Item<ViewHolder>(){
    override fun bind(viewHolder: ViewHolder, position: Int) {
        TODO("Not yet implemented")
    }

    override fun getLayout(): Int {
        return R.layout.chat_from_row
    }
}

class ChatToItem(val text:String): Item<ViewHolder>(){
    override fun bind(viewHolder: ViewHolder, position: Int) {
        TODO("Not yet implemented")
    }

    override fun getLayout(): Int {
        return R.layout.chat_from_row
    }
}