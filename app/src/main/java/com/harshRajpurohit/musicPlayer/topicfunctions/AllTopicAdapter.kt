package com.harshRajpurohit.musicPlayer.topicfunctions

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.harshRajpurohit.musicPlayer.MainActivity
import com.harshRajpurohit.musicPlayer.Music
import com.harshRajpurohit.musicPlayer.databinding.AllTopicItemBinding
import com.harshRajpurohit.musicPlayer.sendTopicMusicData

class AllTopicAdapter(private val context: Context, private val topicList: List<Topic>) :
    RecyclerView.Adapter<AllTopicAdapter.AllTopicViewHolder>() {

    lateinit var topicMusicList: MutableList<Music>

    companion object {
        var topicVpopMusicList: MutableList<Music>
        var topicUsUkMusicList: MutableList<Music>
        var topicBoleroMusicList: MutableList<Music>
        var topicRemixMusicList: MutableList<Music>
        init {
            topicVpopMusicList = mutableListOf()
            topicUsUkMusicList = mutableListOf()
            topicBoleroMusicList = mutableListOf()
            topicRemixMusicList = mutableListOf()
            val totalSong = MainActivity.MusicListMA
            for (song in totalSong){
                if (song.topic == "Nhạc Việt Hot") {
                    topicVpopMusicList.add(song)
                }
                if (song.topic == "Nhạc Ngoại") {
                    topicUsUkMusicList.add(song)
                }
                if (song.topic == "Nhạc trữ tình") {
                    topicBoleroMusicList.add(song)
                }
                if (song.topic == "Nhạc remix") {
                    topicRemixMusicList.add(song)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AllTopicViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = AllTopicItemBinding.inflate(inflater, parent, false)
        return AllTopicViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AllTopicViewHolder, position: Int) {
        val topic = topicList[position]
        holder.bind(topic)
        when (position) {
            0 -> {
                holder.itemView.setOnClickListener {
                    sendTopicMusicData(context,topicVpopMusicList, "Nhạc Việt Hot")
                }
            }

            1 -> {
                holder.itemView.setOnClickListener {
                    sendTopicMusicData(context,topicUsUkMusicList, "Nhạc Ngoại")
                }
            }

            2 -> {
                holder.itemView.setOnClickListener {
                    sendTopicMusicData(context,topicBoleroMusicList, "Nhạc trữ tình")
                }
            }

            3 -> {
                holder.itemView.setOnClickListener {
                    sendTopicMusicData(context,topicRemixMusicList, "Nhạc remix")
                }
            }

            else -> {
                holder.itemView.setOnClickListener(null)
            }
        }
    }


    override fun getItemCount(): Int {
        return topicList.size
    }

    inner class AllTopicViewHolder(private val binding: AllTopicItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(topic: Topic) {
            binding.apply {
                tvAllTopic.text = topic.topicName
                imageViewAllTopic.setImageResource(topic.imageResource)
            }
        }
    }

}
