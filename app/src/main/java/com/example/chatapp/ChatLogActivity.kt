package com.example.chatapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import com.xwray.groupie.ViewHolder
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_chat_log.*
import kotlinx.android.synthetic.main.chat_from_row.view.*
import kotlinx.android.synthetic.main.chat_to_row.view.*
import kotlinx.android.synthetic.main.chat_to_row.view.back_to_register_textview
import timber.log.Timber

class ChatLogActivity : AppCompatActivity() {

    val adapter=GroupAdapter<ViewHolder>()
    val toUser:User?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)
        //val username=intent.getStringExtra(NewMessageActivity.USER_KEY)
        val toUser=intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
        supportActionBar?.title=toUser?.username
        recyclerview_chat_log.adapter=adapter

        listenForMessages()



        send_button_chat_log.setOnClickListener {
            Timber.d("Attempting to Send message")
            performSendMessage()
        }
    }

    private fun listenForMessages() {
        val fromID=FirebaseAuth.getInstance().uid
        val toID=toUser?.uid
        val ref=FirebaseDatabase.getInstance().getReference("/user-messages/$fromID/$toID")

        ref.addChildEventListener(object :ChildEventListener{
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val chatMessage=snapshot.getValue(ChatMessage::class.java)
                Timber.d(chatMessage?.text)
                if(chatMessage!=null){
                    if(chatMessage.fromID==FirebaseAuth.getInstance().uid){
                        val currentUser=LatestMessagesActivity.currentUser
                        adapter.add(ChatToItem(chatMessage.text,currentUser!!))
                    }else{
                        adapter.add(ChatFromItem(chatMessage.text,toUser!!))
                    }

                }
                recyclerview_chat_log.scrollToPosition(adapter.itemCount-1)

            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                //Nothing to add
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                //Nothing to add
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                //Nothing to add
            }

            override fun onCancelled(error: DatabaseError) {
                //Nothing to add
            }
        })
    }

    class ChatMessage(val id:String,val text: String,val fromID:String,val toID:String,val timeStamp:Long){
        constructor():this("","","","",-1)
    }

    private fun performSendMessage() {
        val text=edittext_chat_log.text.toString()
        val fromID=FirebaseAuth.getInstance().uid
        val user=intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
        val toID=user?.uid
        val reference=FirebaseDatabase.getInstance().getReference("/user-messages/$fromID/$toID").push() //push() creates the path
        val toReference=FirebaseDatabase.getInstance().getReference("/user-messages/$toID/$fromID").push()
        val chatMessage=ChatMessage(reference.key!!,text,fromID!!,toID!!,System.currentTimeMillis()/1000)
        reference.setValue(chatMessage)
            .addOnSuccessListener {
                Timber.d("Saved Message ${reference.key}")
                edittext_chat_log.text.clear()
                recyclerview_chat_log.scrollToPosition(adapter.itemCount-1)
            }
        toReference.setValue(chatMessage)

        val latestMessageRef=FirebaseDatabase.getInstance().getReference("/latest-messages/$fromID/$toID")
        latestMessageRef.setValue(chatMessage)

        val latestMessageToRef=FirebaseDatabase.getInstance().getReference("/latest-messages/$toID/$fromID")
        latestMessageToRef.setValue(chatMessage)
    }
}
class ChatFromItem(val text:String,val user:User): Item<ViewHolder>(){
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.textview_from_row.text=text
        val uri=user.profileImageUrl
        val targetImageView=viewHolder.itemView.imageview_from_row
        Picasso.get().load(uri).into(targetImageView)
    }

    override fun getLayout(): Int {
        return R.layout.chat_from_row
    }
}

class ChatToItem(val text:String,val user:User): Item<ViewHolder>(){
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.back_to_register_textview.text=text
        val uri=user.profileImageUrl
        val targetImageView=viewHolder.itemView.imageview_to_row
        Picasso.get().load(uri).into(targetImageView)
    }

    override fun getLayout(): Int {
        return R.layout.chat_to_row
    }
}