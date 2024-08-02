package com.harshRajpurohit.musicPlayer.albumfunctions

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.harshRajpurohit.musicPlayer.MainActivity
import com.harshRajpurohit.musicPlayer.Music
import com.harshRajpurohit.musicPlayer.MusicAdapter
import com.harshRajpurohit.musicPlayer.databinding.ActivityAlbumListSongBinding

class AlbumListSongActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAlbumListSongBinding

    companion object {
        lateinit var albumListSong: MutableList<Music>
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(MainActivity.currentTheme[MainActivity.themeIndex])
        binding = ActivityAlbumListSongBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupData()
        setupToolbar()
    }

    private fun setupToolbar() {
        val topicToolBar = binding.toolBarAlbumListSong
        setSupportActionBar(topicToolBar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        val collapsingToolbarLayout = binding.collapseToolBar
        collapsingToolbarLayout.setExpandedTitleColor(Color.WHITE)
        collapsingToolbarLayout.setCollapsedTitleTextColor(Color.WHITE)
        topicToolBar.setNavigationOnClickListener { finish() }
    }

    @Suppress("DEPRECATION")
    private fun setupData() {
        albumListSong = intent.getParcelableArrayListExtra("AlbumListSong")!!
        Glide.with(this)
            .load(albumListSong.first().artUri)
            .into(object : CustomTarget<Drawable>() {
                override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                    binding.collapseToolBar.background = resource
                }

                override fun onLoadCleared(placeholder: Drawable?) {

                }
            })
        Glide.with(this)
            .load(albumListSong.first().artUri)
            .into(binding.imageViewAlbumListSong)
        binding.recyclerViewAlbumListSong.setHasFixedSize(true)
        binding.recyclerViewAlbumListSong.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewAlbumListSong.adapter =
            MusicAdapter(this, albumListSong, albumSongActivity = true)
    }
}