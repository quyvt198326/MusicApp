package com.harshRajpurohit.musicPlayer.albumfunctions

import com.harshRajpurohit.musicPlayer.MainActivity
import com.harshRajpurohit.musicPlayer.Music

data class Album(
    val albumName: String, val albumArtist: String, val albumImage: String,
    val albumListSong: MutableList<Music>
)

fun loadAlbumData(): List<Album> {
    val songsByAlbum = MainActivity.MusicListMA.groupBy { it.album }

// Lọc ra các album có nhiều hơn một bài hát và tạo danh sách các đối tượng Album
    val albumsWithMultipleSongs: List<Album> =
        songsByAlbum.filter { it.value.size > 1 }.map { (albumName, songs) ->
            val albumArtist = songs.first().artist
            val albumImage = songs.first().artUri
            Album(albumName!!, albumArtist!!, albumImage!!, songs.toMutableList())
        }

    return albumsWithMultipleSongs
}


