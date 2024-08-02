package com.harshRajpurohit.musicPlayer.albumfunctions

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.harshRajpurohit.musicPlayer.databinding.AlbumItemBinding

class AlbumAdapter(private val context: Context, private val albumList: List<Album>) :
    RecyclerView.Adapter<AlbumAdapter.AlbumViewHolder>() {

    inner class AlbumViewHolder(private val binding: AlbumItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(album: Album) {
            binding.tvTenAlbum.text = album.albumName
            binding.tvTenCaSiAlbum.text = album.albumArtist

            Glide.with(context)
                .load(album.albumImage)
                .into(binding.imageviewalbum)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumViewHolder {
        val binding = AlbumItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AlbumViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AlbumViewHolder, position: Int) {
        holder.bind(albumList[position])
        holder.itemView.setOnClickListener {
            val intent = Intent(context, AlbumListSongActivity::class.java)
            intent.putParcelableArrayListExtra(
                "AlbumListSong",
                ArrayList(albumList[position].albumListSong)
            )
            ContextCompat.startActivity(context, intent, null)
        }
    }

    override fun getItemCount(): Int {
        return albumList.size
    }
}