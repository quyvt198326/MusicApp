package com.harshRajpurohit.musicPlayer.topicfunctions

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.harshRajpurohit.musicPlayer.MainActivity
import com.harshRajpurohit.musicPlayer.R
import com.harshRajpurohit.musicPlayer.databinding.ActivityAllTopicBinding

class AllTopicActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAllTopicBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(MainActivity.currentTheme[MainActivity.themeIndex])
        binding = ActivityAllTopicBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initLayout()
        getData()
    }

    private fun getData() {
        val recyclerViewAllCategory = binding.recyclerViewAllCategory
        val topicList = listOf(
            Topic("Nhạc Việt Hot", R.drawable.vpop),
            Topic("Nhạc US-UK", R.drawable.usuk_logo),
            Topic("Nhạc Trữ Tình", R.drawable.nhacvang),
            Topic("Nhạc Remix", R.drawable.remixlogo)
        )
        recyclerViewAllCategory.setHasFixedSize(true)
        recyclerViewAllCategory.layoutManager = LinearLayoutManager(this)
        recyclerViewAllCategory.adapter = AllTopicAdapter(this, topicList)
    }


    private fun initLayout() {
        val toolBarAllCategory = binding.toolBarAllCategory
        setSupportActionBar(toolBarAllCategory)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.title = "Tất cả chủ đề"
        toolBarAllCategory.setTitleTextColor(getResources().getColor(R.color.purple_200))
        toolBarAllCategory.setNavigationOnClickListener({ finish() })
    }
}