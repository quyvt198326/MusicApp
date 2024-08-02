package com.harshRajpurohit.musicPlayer.dialogFlow

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.api.gax.core.FixedCredentialsProvider
import com.google.auth.oauth2.GoogleCredentials
import com.google.auth.oauth2.ServiceAccountCredentials
import com.google.cloud.dialogflow.v2.DetectIntentRequest
import com.google.cloud.dialogflow.v2.DetectIntentResponse
import com.google.cloud.dialogflow.v2.QueryInput
import com.google.cloud.dialogflow.v2.SessionName
import com.google.cloud.dialogflow.v2.SessionsClient
import com.google.cloud.dialogflow.v2.SessionsSettings
import com.harshRajpurohit.musicPlayer.MainActivity
import com.harshRajpurohit.musicPlayer.Music
import com.harshRajpurohit.musicPlayer.R
import com.harshRajpurohit.musicPlayer.databinding.ActivityChatBinding
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class ChatActivity : AppCompatActivity() {
    private var messageList: ArrayList<Message> = ArrayList()
    private lateinit var binding: ActivityChatBinding
    private var replyReturnResult: Boolean = false


    //dialogFlow
    private var sessionsClient: SessionsClient? = null
    private var sessionName: SessionName? = null
    private val uuid = UUID.randomUUID().toString()
    private val TAG = "chatactivity"
    private lateinit var chatAdapter: ChatAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(MainActivity.currentTheme[MainActivity.themeIndex])
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //setting adapter to recyclerview
        chatAdapter = ChatAdapter(messageList)
        binding.chatView.adapter = chatAdapter

        //onclick listener to update the list and call dialogflow
        binding.btnSend.setOnClickListener {
            val message: String = binding.editMessage.text.toString()
            if (message.isNotEmpty()) {
                addMessageToList(message, false)
                sendMessageToBot(message)
                binding.editMessage.setHint(null)
            } else {
                Toast.makeText(this, "Làm ơn hãy nhập tin nhắn!", Toast.LENGTH_SHORT).show()
            }
        }

        //initialize bot config
        setUpBot()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun addMessageToList(message: String, isReceived: Boolean) {
        messageList.add(Message(message, isReceived))
        binding.editMessage.setText("")
        chatAdapter.notifyDataSetChanged()
        binding.chatView.layoutManager?.scrollToPosition(messageList.size - 1)
    }

    private fun setUpBot() {
        try {
            val stream = this.resources.openRawResource(R.raw.credential)
            val credentials: GoogleCredentials = GoogleCredentials.fromStream(stream)
                .createScoped("https://www.googleapis.com/auth/cloud-platform")
            val projectId: String = (credentials as ServiceAccountCredentials).projectId
            val settingsBuilder: SessionsSettings.Builder = SessionsSettings.newBuilder()
            val sessionsSettings: SessionsSettings = settingsBuilder.setCredentialsProvider(
                FixedCredentialsProvider.create(credentials)
            ).build()
            sessionsClient = SessionsClient.create(sessionsSettings)
            sessionName = SessionName.of(projectId, uuid)
            Log.d(TAG, "projectId : $projectId")
        } catch (e: Exception) {
            Log.d(TAG, "setUpBot: " + e.message)
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun sendMessageToBot(message: String) {
        val input = QueryInput.newBuilder()
            .setText(
                com.google.cloud.dialogflow.v2.TextInput.newBuilder().setText(message)
                    .setLanguageCode("en-US")
            ).build()
        GlobalScope.launch {
            sendMessageInBg(input)
        }
    }

    private suspend fun sendMessageInBg(
        queryInput: QueryInput
    ) {
        withContext(Dispatchers.Default) {
            try {
                val detectIntentRequest = DetectIntentRequest.newBuilder()
                    .setSession(sessionName.toString())
                    .setQueryInput(queryInput)
                    .build()
                val result = sessionsClient?.detectIntent(detectIntentRequest)
                if (result != null) {
                    runOnUiThread {
                        updateUI(result)
                    }
                }
            } catch (e: java.lang.Exception) {
                Log.d(TAG, "doInBackground: " + e.message)
                e.printStackTrace()
            }
        }
    }

    private fun updateUI(response: DetectIntentResponse) {
        val botReply: String = response.queryResult.fulfillmentText
        if (botReply.isNotEmpty()) {
            addMessageToList(botReply, true)
            Log.d("BOT_REPLY", "Bot's reply: $botReply")
            val analysisSongs = analysisBotReply(botReply)
            sendAnalysisSongs(analysisSongs)
        } else {
            Toast.makeText(this, "something went wrong", Toast.LENGTH_SHORT).show()
        }
    }


    private fun analysisBotReply(botReply: String): MutableList<Music> {
        val totalSongs = MainActivity.MusicListMA
        val recommendedSongs = mutableListOf<Music>()
        for (song in totalSongs) {
            if (botReply.contains("buồn") && song.mood == "buồn") {
                if (botReply.contains("trừ nhạc của Erik")) {
                    if(song.artist != "Erik"){
                        recommendedSongs.add(song)
                    }
                }else{
                    recommendedSongs.add(song)
                }
            }
            if (botReply.contains("niềm vui") && song.mood == "vui") {
                recommendedSongs.add(song)
            }
            if (botReply.contains("tức giận") && song.mood == "tức giận") {
                recommendedSongs.add(song)
            }
            if (botReply.contains("trữ tình") && song.topic == "Nhạc trữ tình"){
                recommendedSongs.add(song)
            }
            if (botReply.contains("vui nhưng")){
                if (botReply.contains("buồn") && song.mood == "buồn2"){
                    recommendedSongs.add(song)
                }
            }

        }

        return recommendedSongs
    }

    private fun sendAnalysisSongs(analysisSongs: MutableList<Music>) {
        if (analysisSongs.isNotEmpty()) {
            Handler(Looper.getMainLooper()).postDelayed({
                Log.d("send_song", "results: $analysisSongs")
                val intent = Intent(this, RecommendActivity::class.java)
                intent.putParcelableArrayListExtra("RecommendMusicList", ArrayList(analysisSongs))
                startActivity(intent)
            }, 3000)
        }
        else{
            Toast.makeText(this, "Không tìm thấy bài hát phù hợp", Toast.LENGTH_SHORT).show()
        }
    }

//    data class MusicList(val songs: MutableList<Music>)
}




