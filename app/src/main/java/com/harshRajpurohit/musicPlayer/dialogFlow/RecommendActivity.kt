package com.harshRajpurohit.musicPlayer.dialogFlow

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.harshRajpurohit.musicPlayer.MainActivity
import com.harshRajpurohit.musicPlayer.Music
import com.harshRajpurohit.musicPlayer.MusicAdapter
import com.harshRajpurohit.musicPlayer.Playlist
import com.harshRajpurohit.musicPlayer.PlaylistActivity
import com.harshRajpurohit.musicPlayer.R
import com.harshRajpurohit.musicPlayer.databinding.ActivityRecommendBinding
import com.harshRajpurohit.musicPlayer.databinding.AddPlaylistDialogBinding
import com.harshRajpurohit.musicPlayer.setDialogBtnBackground
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class RecommendActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRecommendBinding
    private lateinit var adapter: MusicAdapter
    private lateinit var originalRecommendList: MutableList<Music>
    companion object{
        var recommendList: MutableList<Music> = mutableListOf()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(MainActivity.currentTheme[MainActivity.themeIndex])
        binding = ActivityRecommendBinding.inflate(layoutInflater)
        setContentView(binding.root)

        @Suppress("DEPRECATION")
        originalRecommendList  = intent.getParcelableArrayListExtra<Music>("RecommendMusicList")!!
        recommendList.clear()
        recommendList.addAll(originalRecommendList)
        Log.d("recommend_activity", "results: $recommendList")
        recommendList.let {
            binding.recommendRV.setHasFixedSize(true)
            binding.recommendRV.setItemViewCacheSize(10)
            binding.recommendRV.layoutManager = LinearLayoutManager(this)
            adapter = MusicAdapter(this, recommendList, recommendActivity = true)
            binding.recommendRV.adapter= adapter
        }

        binding.reloadBtn.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Gợi ý bài hát")
                .setMessage("Bạn có muốn hệ thống gợi ý lại những bài hát hay không?")
                .setPositiveButton("Có") { dialog, _ ->
                    reloadRecommendation()
                    dialog.dismiss()
                }
                .setNegativeButton("Không") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }

        binding.addBtn.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Thêm vào playlist")
                .setMessage("Bạn có muốn tạo một playlist từ những bài hát gợi ý hay không ?")
                .setPositiveButton("Có"){dialog, _ ->
                    createPlaylist(recommendList)
                    dialog.dismiss()
                }
                .setNegativeButton("Không"){dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }

    }

    @SuppressLint("NotifyDataSetChanged")
    private fun reloadRecommendation() {
        val newRecommendList = mutableListOf<Music>()
        for (song in recommendList){
            if (song.mood == "buồn"){
                for (song2 in MainActivity.MusicListMA){
                    if (song2.mood == "buồn2"){
                        newRecommendList.add(song2)
                    }
                }
                break
            }
            else if (song.mood == "vui"){
                for (song2 in MainActivity.MusicListMA){
                    if (song2.mood == "vui2"){
                        newRecommendList.add(song2)
                    }
                }
                break
            }
            else if (song.mood == "tức giận"){
                for (song2 in MainActivity.MusicListMA){
                    if (song2.mood == "tức giận2"){
                        newRecommendList.add(song2)
                    }
                }
                break
            }
            else{
                newRecommendList.addAll(originalRecommendList)
                break
            }
        }
        recommendList.clear()
        recommendList.addAll(newRecommendList)
        adapter.notifyDataSetChanged()
        Toast.makeText(this, "Hệ thống đã cập nhật lại bài hát", Toast.LENGTH_SHORT).show()
    }

    private fun createPlaylist(recommendList: MutableList<Music>){
        val customDialog = LayoutInflater.from(this).inflate(R.layout.add_playlist_dialog, binding.root, false)
        val binder = AddPlaylistDialogBinding.bind(customDialog)
        val builder = MaterialAlertDialogBuilder(this)
        val dialog = builder.setView(customDialog)
            .setTitle("Playlist Details")
            .setPositiveButton("Thêm"){ dialog, _ ->
                val playlistName = binder.playlistName.text
                val createdBy = binder.yourName.text
                if(playlistName != null && createdBy != null)
                    if(playlistName.isNotEmpty() && createdBy.isNotEmpty())
                    {
                        addPlaylist(playlistName.toString(), createdBy.toString(), recommendList)
                    }
                dialog.dismiss()
            }.create()
        dialog.show()
        setDialogBtnBackground(this, dialog)
    }

    private fun addPlaylist(name: String, createdBy: String, recommendList: MutableList<Music>) {
        var playlistExists = false
        for(i in PlaylistActivity.musicPlaylist.ref) {
            if (name == i.name){
                playlistExists = true
                break
            }
        }
        if(playlistExists) Toast.makeText(this, "Playlist đã tồn tại!!", Toast.LENGTH_SHORT).show()
        else {
            val tempPlaylist = Playlist()
            tempPlaylist.name = name
            tempPlaylist.playlist = recommendList
            tempPlaylist.createdBy = createdBy
            val calendar = Calendar.getInstance().time
            val sdf = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
            tempPlaylist.createdOn = sdf.format(calendar)
            PlaylistActivity.musicPlaylist.ref.add(tempPlaylist)
            val intent = Intent(this, PlaylistActivity::class.java)
            startActivity(intent)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

}