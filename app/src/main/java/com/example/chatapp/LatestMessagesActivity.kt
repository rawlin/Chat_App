package com.example.chatapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.DividerItemDecoration
import com.example.chatapp.NewMessageActivity.Companion.USER_KEY
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_latest_messages.*
import kotlinx.android.synthetic.main.latest_message_row.view.*
import timber.log.Timber

class LatestMessagesActivity : AppCompatActivity() {
    companion object{
        var currentUser:User?=null
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_latest_messages)
        recyclerview_latest_messages.adapter=adapter
        recyclerview_latest_messages.addItemDecoration(DividerItemDecoration(this,
        DividerItemDecoration.VERTICAL))

        adapter.setOnItemClickListener{item,view->
            Timber.d("")
            val intent=Intent(this,ChatLogActivity::class.java)

            val row=item as LatestMessageRow

            intent.putExtra(USER_KEY,row.chatPartnerUser)
            startActivity(intent)

        }

        listenForLatestMessages()
        fetchCurrentUser()
        verifyUserIsLoggedIn()

    }
    val latestMessagesMap=HashMap<String,ChatLogActivity.ChatMessage>()

    private fun refreshRecyclerViewMessages(){
        latestMessagesMap.values.forEach {
            adapter.add(LatestMessageRow(it))
        }
    }

    private fun listenForLatestMessages() {
        val fromID=FirebaseAuth.getInstance().uid
        val ref=FirebaseDatabase.getInstance().getReference("/latest-messages/$fromID")
        ref.addChildEventListener(object:ChildEventListener{
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val chatMessage=snapshot.getValue(ChatLogActivity.ChatMessage::class.java)
                latestMessagesMap[snapshot.key!!]= chatMessage!!
                refreshRecyclerViewMessages()

            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val chatMessage=snapshot.getValue(ChatLogActivity.ChatMessage::class.java)
                latestMessagesMap[snapshot.key!!]= chatMessage!!
                refreshRecyclerViewMessages()
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
    val adapter=GroupAdapter<ViewHolder>()

    class LatestMessageRow(val chatMessage: ChatLogActivity.ChatMessage): Item<ViewHolder>(){

        var chatPartnerUser:User?=null
        override fun bind(viewHolder: ViewHolder, position: Int) {
            viewHolder.itemView.message_textview_latest_message.text=chatMessage.text
            val chatPartnerID:String
            if(chatMessage.fromID==FirebaseAuth.getInstance().uid){
                chatPartnerID=chatMessage.toID
            }else{
                chatPartnerID=chatMessage.fromID
            }

            val ref=FirebaseDatabase.getInstance().getReference("/users/$chatPartnerID")
            ref.addListenerForSingleValueEvent(object :ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    chatPartnerUser=snapshot.getValue(User::class.java)

                    viewHolder.itemView.username_textview_latest_message.text=chatPartnerUser?.username
                    val target=viewHolder.itemView.imageview_latest_message
                    Picasso.get().load(chatPartnerUser?.profileImageUrl).into(target)
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })

        }

        override fun getLayout(): Int {
            return R.layout.latest_message_row
        }
    }

    private fun fetchCurrentUser() {
        val uid=FirebaseAuth.getInstance().uid
        val ref=FirebaseDatabase.getInstance().getReference("/users/$uid")
        ref.addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                currentUser=snapshot.getValue(User::class.java)
                Timber.d("CurrentUser ${currentUser?.username}")
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    private fun verifyUserIsLoggedIn() {
        val uid=FirebaseAuth.getInstance().uid
        if(uid==null){
            val intent=Intent(this,RegisterActivity::class.java)
            intent.flags=Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK) //Clearing Activities in the BackStack
            startActivity(intent)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.nav_menu,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item?.itemId){
            R.id.menu_new_message->{
                val intent=Intent(this,NewMessageActivity::class.java)
                startActivity(intent)
            }
            R.id.menu_sign_out->{
                FirebaseAuth.getInstance().signOut()
                val intent=Intent(this,RegisterActivity::class.java)
                intent.flags=Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK) //Clearing Activities in the BackStack
                startActivity(intent)
            }
        }

        return super.onOptionsItemSelected(item)
    }
}