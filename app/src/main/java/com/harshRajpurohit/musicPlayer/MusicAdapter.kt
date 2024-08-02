package com.harshRajpurohit.musicPlayer

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.text.SpannableStringBuilder
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.text.bold
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.harshRajpurohit.musicPlayer.databinding.DetailsViewBinding
import com.harshRajpurohit.musicPlayer.databinding.MoreFeaturesBinding
import com.harshRajpurohit.musicPlayer.databinding.MusicViewBinding

class MusicAdapter(private val context: Context, private var musicList: MutableList<Music>, private val playlistDetails: Boolean = false,
                   private val selectionActivity: Boolean = false, private val recommendActivity: Boolean = false,
                    private val topicListActivity: Boolean = false, private val albumSongActivity: Boolean = false)
    : RecyclerView.Adapter<MusicAdapter.MyHolder>() {

    interface OnItemClickListener {
        fun onItemClick(song: Music)
    }

    class MyHolder(val binding: MusicViewBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        val binding = MusicViewBinding.inflate(LayoutInflater.from(context), parent, false)
        return MyHolder(binding)
    }

    override fun onBindViewHolder(holder: MyHolder, position: Int) {
        val music = musicList[position]
        holder.binding.songNameMV.text = music.title
        holder.binding.songArtistMV.text = music.artist
        holder.binding.songDuration.text = formatDurationAdapter(music.duration)
        Glide.with(context)
            .load(music.artUri)
            .apply(RequestOptions().placeholder(R.drawable.music_player_icon_slash_screen).centerCrop())
            .into(holder.binding.imageMV)

        holder.binding.root.setOnLongClickListener {
            val customDialog = LayoutInflater.from(context).inflate(R.layout.more_features, holder.binding.root, false)
            val bindingMF = MoreFeaturesBinding.bind(customDialog)
            val dialog = MaterialAlertDialogBuilder(context).setView(customDialog)
                .create()
            dialog.show()
            dialog.window?.setBackgroundDrawable(ColorDrawable(0x99000000.toInt()))

            bindingMF.AddToPNBtn.setOnClickListener {
                try {
                    if(PlayNext.playNextList.isEmpty()){
                        PlayNext.playNextList.add(PlayerActivity.musicListPA[PlayerActivity.songPosition])
                        PlayerActivity.songPosition = 0
                    }

                    PlayNext.playNextList.add(music)
                    PlayerActivity.musicListPA = ArrayList()
                    PlayerActivity.musicListPA.addAll(PlayNext.playNextList)
                }catch (e: Exception){
                    Snackbar.make(context, holder.binding.root,"Hãy phát một bài hát trước đó!!", 3000).show()
                }
                dialog.dismiss()
            }

            bindingMF.infoBtn.setOnClickListener {
                dialog.dismiss()
                val detailsDialog = LayoutInflater.from(context).inflate(R.layout.details_view, bindingMF.root, false)
                val binder = DetailsViewBinding.bind(detailsDialog)
                binder.detailsTV.setTextColor(Color.WHITE)
                binder.root.setBackgroundColor(Color.TRANSPARENT)
                val dDialog = MaterialAlertDialogBuilder(context)
                    .setView(detailsDialog)
                    .setPositiveButton("OK"){self, _ -> self.dismiss()}
                    .setCancelable(false)
                    .create()
                dDialog.show()
                dDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.RED)
                setDialogBtnBackground(context, dDialog)
                dDialog.window?.setBackgroundDrawable(ColorDrawable(0x99000000.toInt()))
                val str = SpannableStringBuilder().bold { append("DETAILS\n\nName: ") }
                    .append(music.title)
                    .bold { append("\n\nDuration: ") }.append(DateUtils.formatElapsedTime(music.duration/1000))
                    .bold { append("\n\nLocation: ") }.append(music.path)
                binder.detailsTV.text = str
            }

            return@setOnLongClickListener true
        }

        when {
            playlistDetails -> {
                holder.binding.root.setOnClickListener {
                    sendIntent(ref = "PlaylistDetailsAdapter", pos = position)
                }
            }
            selectionActivity -> {
                holder.binding.root.setOnClickListener {
                    if(addSong(music))
                        holder.binding.root.setBackgroundColor(ContextCompat.getColor(context, R.color.cool_pink))
                    else
                        holder.binding.root.setBackgroundColor(ContextCompat.getColor(context, R.color.white))
                }
            }
            recommendActivity -> {
                holder.binding.root.setOnClickListener {
                    sendIntent(ref = "RecommendActivity", pos = position)
                }
            }
            topicListActivity -> {
                holder.binding.root.setOnClickListener {
                    sendIntent(ref = "TopicListActivity", pos = position)
                }
            }
            albumSongActivity -> {
                holder.binding.root.setOnClickListener {
                    sendIntent(ref = "AlbumSongActivity", pos = position)
                }
            }
            else -> {
                holder.binding.root.setOnClickListener {
                    when {
                        MainActivity.search -> sendIntent(ref = "MusicAdapterSearch", pos = position)
                        music.id == PlayerActivity.nowPlayingId ->
                            sendIntent(ref = "NowPlaying", pos = PlayerActivity.songPosition)
                        else -> sendIntent(ref="MusicAdapter", pos = position)
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return musicList.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateMusicList(searchList : MutableList<Music>){
        musicList = mutableListOf()
        musicList.addAll(searchList)
        notifyDataSetChanged()
    }

    private fun sendIntent(ref: String, pos: Int){
        val intent = Intent(context, PlayerActivity::class.java)
        intent.putExtra("index", pos)
        intent.putExtra("class", ref)
        ContextCompat.startActivity(context, intent, null)
    }

    private fun addSong(song: Music): Boolean{
        PlaylistActivity.musicPlaylist.ref[PlaylistDetails.currentPlaylistPos].playlist.forEachIndexed { index, music ->
            if(song.id == music.id){
                PlaylistActivity.musicPlaylist.ref[PlaylistDetails.currentPlaylistPos].playlist.removeAt(index)
                return false
            }
        }
        PlaylistActivity.musicPlaylist.ref[PlaylistDetails.currentPlaylistPos].playlist.add(song)
        return true
    }

    @SuppressLint("NotifyDataSetChanged")
    fun refreshPlaylist(){
        musicList = mutableListOf()
        musicList = PlaylistActivity.musicPlaylist.ref[PlaylistDetails.currentPlaylistPos].playlist
        notifyDataSetChanged()
    }
}
