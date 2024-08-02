package com.harshRajpurohit.musicPlayer

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.media.MediaMetadataRetriever
import android.os.Parcel
import android.os.Parcelable
import androidx.appcompat.app.AlertDialog
import com.google.android.material.color.MaterialColors
import com.google.gson.annotations.SerializedName
import org.json.JSONObject
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.system.exitProcess
import android.app.DownloadManager
import android.content.Intent
import android.net.Uri
import android.os.Environment
import androidx.core.content.ContextCompat
import com.harshRajpurohit.musicPlayer.topicfunctions.TopicListActivity


data class Music(
    @SerializedName("id") var id: String?,
    @SerializedName("title") var title: String?,
    @SerializedName("album") var album: String?,
    @SerializedName("artist") var artist: String?,
    @SerializedName("duration") var duration: Long = 0,
    @SerializedName("source") var path: String?,
    @SerializedName("image") var artUri: String?,
    @SerializedName("mood") var mood: String?,
    @SerializedName("topic") var topic: String?
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readLong(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(title)
        parcel.writeString(album)
        parcel.writeString(artist)
        parcel.writeLong(duration)
        parcel.writeString(path)
        parcel.writeString(artUri)
        parcel.writeString(mood)
        parcel.writeString(topic)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Music> {
        override fun createFromParcel(parcel: Parcel): Music {
            return Music(parcel)
        }

        override fun newArray(size: Int): Array<Music?> {
            return arrayOfNulls(size)
        }
    }
}

fun loadSongsFromJson(context: Context): MutableList<Music> {
    val inputStream = context.resources.openRawResource(R.raw.songs)
    val jsonString = inputStream.bufferedReader().use { it.readText() }
    inputStream.close()

    val jsonObject = JSONObject(jsonString)
    val songsArray = jsonObject.getJSONArray("song")
    val songsList = mutableListOf<Music>()

    for (i in 0 until songsArray.length()) {
        val songObject = songsArray.getJSONObject(i)
        val sourceUrl = songObject.getString("source")
        val sourceFileName = "song_$i.mp3" // Generate a unique file name for each song

        val musicFile =
            File(context.getExternalFilesDir(Environment.DIRECTORY_MUSIC), sourceFileName)

        if (musicFile.exists()) {
            // If music file already exists, use it directly
            val song = Music(
                songObject.getString("id"),
                songObject.getString("title"),
                songObject.getString("album"),
                songObject.getString("artist"),
                songObject.getLong("duration"),
                musicFile.path, // Use the local path instead of source URL
                songObject.getString("image"),
                songObject.getString("mood"),
                songObject.getString("topic")
            )
            songsList.add(song)
        } else {
            // If music file doesn't exist, start download
            startDownload(context, sourceUrl, sourceFileName)
        }
    }

    return songsList
}

private fun startDownload(
    context: Context,
    urlString: String,
    fileName: String,
    ) {

    val request = DownloadManager.Request(Uri.parse(urlString))
    request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
    request.setTitle("Downloading music...")
    request.setDestinationInExternalFilesDir(context, Environment.DIRECTORY_MUSIC, fileName)

    val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    downloadManager.enqueue(request)

}


class Playlist {
    lateinit var name: String
    lateinit var playlist: MutableList<Music>
    lateinit var createdBy: String
    lateinit var createdOn: String
}

class MusicPlaylist {
    var ref: MutableList<Playlist> = mutableListOf()
}

fun formatDuration(duration: Long): String {
    val minutes = TimeUnit.MINUTES.convert(duration, TimeUnit.MILLISECONDS)
    val seconds = (TimeUnit.SECONDS.convert(duration, TimeUnit.MILLISECONDS) -
            minutes * TimeUnit.SECONDS.convert(1, TimeUnit.MINUTES))
    return String.format("%02d:%02d", minutes, seconds)
}

fun getImgArt(path: String): ByteArray? {
    val retriever = MediaMetadataRetriever()
    retriever.setDataSource(path)
    return retriever.embeddedPicture
}

fun setSongPosition(increment: Boolean) {
    if (!PlayerActivity.repeat) {
        if (increment) {
            if (PlayerActivity.musicListPA.size - 1 == PlayerActivity.songPosition)
                PlayerActivity.songPosition = 0
            else ++PlayerActivity.songPosition
        } else {
            if (0 == PlayerActivity.songPosition)
                PlayerActivity.songPosition = PlayerActivity.musicListPA.size - 1
            else --PlayerActivity.songPosition
        }
    }
}

fun exitApplication() {
    if (PlayerActivity.musicService != null) {
        PlayerActivity.musicService!!.audioManager.abandonAudioFocus(PlayerActivity.musicService)
        PlayerActivity.musicService!!.stopForeground(true)
        PlayerActivity.musicService!!.mediaPlayer!!.release()
        PlayerActivity.musicService = null
    }
    exitProcess(1)
}

fun favouriteChecker(id: String): Int {
    PlayerActivity.isFavourite = false
    FavouriteActivity.favouriteSongs.forEachIndexed { index, music ->
        if (id == music.id) {
            PlayerActivity.isFavourite = true
            return index
        }
    }
    return -1
}

fun checkPlaylist(playlist: MutableList<Music>): MutableList<Music> {
    playlist.forEachIndexed { index, music ->
        val file = File(music.path)
        if (!file.exists())
            playlist.removeAt(index)
    }
    return playlist
}

fun setDialogBtnBackground(context: Context, dialog: AlertDialog) {
    //setting button text
    dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE)?.setTextColor(
        MaterialColors.getColor(context, R.attr.dialogTextColor, Color.WHITE)
    )
    dialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE)?.setTextColor(
        MaterialColors.getColor(context, R.attr.dialogTextColor, Color.WHITE)
    )

    //setting button background
    dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE)?.setBackgroundColor(
        MaterialColors.getColor(context, R.attr.dialogBtnBackground, Color.RED)
    )
    dialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE)?.setBackgroundColor(
        MaterialColors.getColor(context, R.attr.dialogBtnBackground, Color.RED)
    )
}

fun formatDurationAdapter(duration: Long): String {
    var minutes = duration / 60
    var seconds = duration % 60 - 1

    if (seconds < 0) {
        minutes -= 1
        seconds = 59
    }
    return String.format("%02d:%02d", minutes, seconds)
}



fun sendTopicMusicData(context: Context, topicMusicList: MutableList<Music>, topic: String) {
    val intent = Intent(context, TopicListActivity::class.java)
    intent.putExtra("topic", topic)
    intent.putParcelableArrayListExtra(topic, ArrayList(topicMusicList))
    ContextCompat.startActivity(context, intent, null)
}


fun getMainColor(img: Bitmap): Int {
    val newImg = Bitmap.createScaledBitmap(img, 1, 1, true)
    val color = newImg.getPixel(0, 0)
    newImg.recycle()
    return color
}