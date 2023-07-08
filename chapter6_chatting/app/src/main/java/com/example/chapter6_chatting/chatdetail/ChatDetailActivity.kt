package com.example.chapter6_chatting.chatdetail

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chapter6_chatting.Key.Companion.DB_CHATS
import com.example.chapter6_chatting.Key.Companion.DB_CHAT_ROOM
import com.example.chapter6_chatting.Key.Companion.DB_USERS
import com.example.chapter6_chatting.R
import com.example.chapter6_chatting.databinding.ActivityChatdetailBinding
import com.example.chapter6_chatting.userlist.UserItem
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class ChatDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatdetailBinding
    private lateinit var chatAdapter: ChatDetailAdapter
    private lateinit var linearLayoutManager: LinearLayoutManager

    private var chatRoomId: String = ""
    private var otherUserId: String = ""
    private var otherUserFcmToken: String = ""
    private var myUserId: String = ""
    private var myUserName: String = ""
    private var isInit = false

    private val chatItemList = mutableListOf<ChatItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatdetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        chatAdapter = ChatDetailAdapter()
        linearLayoutManager = LinearLayoutManager(applicationContext)

        chatRoomId = intent.getStringExtra(EXTRA_CHAT_ROOM_ID) ?: return
        otherUserId = intent.getStringExtra(EXTRA_OTHER_USER_ID) ?: return
        myUserId = Firebase.auth.currentUser?.uid ?: ""

        Firebase.database.reference.child(DB_USERS).child(myUserId).get()
            .addOnSuccessListener {
                val myUserItem = it.getValue(UserItem::class.java)
                myUserName = myUserItem?.username ?: ""

                getOtherUserData()
            }


        binding.chatRecyclerView.apply {
            layoutManager = linearLayoutManager
            adapter = chatAdapter
        }

        chatAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
                super.onItemRangeChanged(positionStart, itemCount)

                linearLayoutManager.smoothScrollToPosition(binding.chatRecyclerView, null, chatAdapter.itemCount)
            }
        })

        binding.sendButton.setOnClickListener {
            val message = binding.messageEditText.text.toString()

            if (isInit == false) {
                return@setOnClickListener
            }

            if (message.isEmpty()) {
                Toast.makeText(applicationContext, "메세지를 입력해주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val newChatItem = ChatItem(
                message = message,
                userId = myUserId
            )

            Firebase.database.reference.child(DB_CHATS).child(chatRoomId).push().apply {
                newChatItem.chatId = key
                setValue(newChatItem)
            }

            val updates: MutableMap<String, Any> = hashMapOf(
                "${DB_CHAT_ROOM}/$myUserId/$otherUserId/lastMessage" to message,
                "${DB_CHAT_ROOM}/$otherUserId/$myUserId/lastMessage" to message,
                "${DB_CHAT_ROOM}/$otherUserId/$myUserId/chatRoomId" to chatRoomId,
                "${DB_CHAT_ROOM}/$otherUserId/$myUserId/otherUserId" to myUserId,
                "${DB_CHAT_ROOM}/$otherUserId/$myUserId/otherUserName" to myUserName,
            )
            Firebase.database.reference.updateChildren(updates)

            val client = OkHttpClient()

            val root = JSONObject()
            val notification = JSONObject()
            notification.put("title", getString(R.string.app_name))
            notification.put("body", message)
            root.put("to", otherUserFcmToken)
            root.put("priority", "high")
            root.put("notification", notification)

            val requestBody = root.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            val request = Request.Builder().post(requestBody).url("https://fcm.googleapis.com/fcm/send")
                .header("Authorization", "key=${getString(R.string.fcm_server_key)}").build()
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) { }

                override fun onResponse(call: Call, response: Response) { }
            })

            binding.messageEditText.text.clear()
        }
    }

    private fun getOtherUserData() {
        Firebase.database.reference.child(DB_USERS).child(otherUserId).get()
            .addOnSuccessListener {
                val otherUserItem = it.getValue(UserItem::class.java)
                otherUserFcmToken = otherUserItem?.fcmToken.orEmpty()
                chatAdapter.otherUserItem = otherUserItem

                isInit = true
                getChatData()
            }
    }

    private fun getChatData() {
        Firebase.database.reference.child(DB_CHATS).child(chatRoomId).addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val chatItem = snapshot.getValue(ChatItem::class.java)
                chatItem ?: return

                chatItemList.add(chatItem)
                chatAdapter.submitList(chatItemList.toMutableList())
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) { }

            override fun onChildRemoved(snapshot: DataSnapshot) { }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) { }

            override fun onCancelled(error: DatabaseError) { }

        })
    }

    companion object {
        const val EXTRA_CHAT_ROOM_ID = "CHAT_ROOM_ID"
        const val EXTRA_OTHER_USER_ID = "OTHER_USER_ID"
    }
}