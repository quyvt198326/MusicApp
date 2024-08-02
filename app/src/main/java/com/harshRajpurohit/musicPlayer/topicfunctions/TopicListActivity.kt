package com.harshRajpurohit.musicPlayer.topicfunctions

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.harshRajpurohit.musicPlayer.MainActivity
import com.harshRajpurohit.musicPlayer.Music
import com.harshRajpurohit.musicPlayer.MusicAdapter
import com.harshRajpurohit.musicPlayer.R
import com.harshRajpurohit.musicPlayer.databinding.ActivityTopicListBinding

class TopicListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTopicListBinding

    companion object {
        lateinit var musicListTopic: MutableList<Music>
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(MainActivity.currentTheme[MainActivity.themeIndex])
        binding = ActivityTopicListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupToolBar()
        val topic = intent.getStringExtra("topic")
        when (topic) {
            "Nhạc Việt Hot" -> {
                musicListTopic = intent.getParcelableArrayListExtra<Music>(topic)!!
                binding.collapseToolBar.setBackgroundResource(R.drawable.vpop)
                binding.imageViewMusicList.setImageResource(R.drawable.vpop)
            }
            "Nhạc Ngoại" -> {
                musicListTopic = intent.getParcelableArrayListExtra<Music>(topic)!!
                binding.collapseToolBar.setBackgroundResource(R.drawable.usuk_logo)
                binding.imageViewMusicList.setImageResource(R.drawable.usuk_logo)
            }
            "Nhạc trữ tình" -> {
                musicListTopic = intent.getParcelableArrayListExtra<Music>(topic)!!
                binding.collapseToolBar.setBackgroundResource(R.drawable.nhacvang)
                binding.imageViewMusicList.setImageResource(R.drawable.nhacvang)
            }
            "Nhạc remix" -> {
                musicListTopic = intent.getParcelableArrayListExtra<Music>(topic)!!
                binding.collapseToolBar.setBackgroundResource(R.drawable.remixlogo)
                binding.imageViewMusicList.setImageResource(R.drawable.remixlogo)
            }
        }

        binding.recyclerViewMusicList.setHasFixedSize(true)
        binding.recyclerViewMusicList.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewMusicList.adapter =
            MusicAdapter(this, musicListTopic, topicListActivity = true)
    }

    private fun setupToolBar() {
        val topicToolBar = binding.toolBarMusicList
        setSupportActionBar(topicToolBar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        val collapsingToolbarLayout = binding.collapseToolBar
        collapsingToolbarLayout.setExpandedTitleColor(Color.WHITE)
        collapsingToolbarLayout.setCollapsedTitleTextColor(Color.WHITE)
        topicToolBar.setNavigationOnClickListener { finish() }
    }
}