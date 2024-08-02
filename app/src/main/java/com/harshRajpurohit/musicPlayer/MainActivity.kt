package com.harshRajpurohit.musicPlayer

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.harshRajpurohit.musicPlayer.albumfunctions.Album
import com.harshRajpurohit.musicPlayer.albumfunctions.AlbumAdapter
import com.harshRajpurohit.musicPlayer.albumfunctions.loadAlbumData
import com.harshRajpurohit.musicPlayer.databinding.ActivityMainBinding
import com.harshRajpurohit.musicPlayer.dialogFlow.ChatActivity
import com.harshRajpurohit.musicPlayer.topicfunctions.AllTopicActivity
import com.harshRajpurohit.musicPlayer.topicfunctions.AllTopicAdapter

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var musicAdapter: MusicAdapter
    private var isSongLoaded: Boolean = false

    companion object {
        lateinit var auth: FirebaseAuth
        lateinit var MusicListMA: MutableList<Music>
        lateinit var musicListSearch: MutableList<Music>
        lateinit var MusicListRL: MutableList<Music>
        var search: Boolean = false
        var searching = false
        var themeIndex: Int = 0
        val currentTheme = arrayOf(
            R.style.coolPink,
            R.style.coolBlue,
            R.style.coolPurple,
            R.style.coolGreen,
            R.style.coolBlack
        )
        val currentThemeNav = arrayOf(
            R.style.coolPinkNav, R.style.coolBlueNav, R.style.coolPurpleNav, R.style.coolGreenNav,
            R.style.coolBlackNav
        )
        val currentGradient = arrayOf(
            R.drawable.gradient_pink,
            R.drawable.gradient_blue,
            R.drawable.gradient_purple,
            R.drawable.gradient_green,
            R.drawable.gradient_black
        )
        var sortOrder: Int = 0
        val sortingList = arrayOf(
            MediaStore.Audio.Media.DATE_ADDED + " DESC", MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.SIZE + " DESC"
        )
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()

        if(auth.currentUser == null){
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()
        }

        val themeEditor = getSharedPreferences("THEMES", MODE_PRIVATE)
        themeIndex = themeEditor.getInt("themeIndex", 0)
        setTheme(currentThemeNav[themeIndex])
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //for nav drawer
        toggle = ActionBarDrawerToggle(this, binding.root, R.string.open, R.string.close)
        binding.root.addDrawerListener(toggle)
        toggle.syncState()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        //checking for dark theme
        if (themeIndex == 4 && resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_NO)
            Toast.makeText(this, "Black Theme Works Best in Dark Mode!!", Toast.LENGTH_LONG).show()

        if (requestRuntimePermission()) {
            initializeLayout()
            //for retrieving favourites data using shared preferences
            FavouriteActivity.favouriteSongs = mutableListOf<Music>()
            val editor = getSharedPreferences("FAVOURITES", MODE_PRIVATE)
            val jsonString = editor.getString("FavouriteSongs", null)
            val typeToken = object : TypeToken<MutableList<Music>>() {}.type
            if (jsonString != null) {
                val data: MutableList<Music> =
                    GsonBuilder().create().fromJson(jsonString, typeToken)
                FavouriteActivity.favouriteSongs.addAll(data)
            }
            PlaylistActivity.musicPlaylist = MusicPlaylist()
            val jsonStringPlaylist = editor.getString("MusicPlaylist", null)
            if (jsonStringPlaylist != null) {
                val dataPlaylist: MusicPlaylist =
                    GsonBuilder().create().fromJson(jsonStringPlaylist, MusicPlaylist::class.java)
                PlaylistActivity.musicPlaylist = dataPlaylist
            }
        }

        binding.chatBtn.setOnClickListener {
            startActivity(Intent(this@MainActivity, ChatActivity::class.java))
        }
        binding.favouriteBtn.setOnClickListener {
            startActivity(Intent(this@MainActivity, FavouriteActivity::class.java))
        }
        binding.playlistBtn.setOnClickListener {
            startActivity(Intent(this@MainActivity, PlaylistActivity::class.java))
        }
        binding.playNextBtn.setOnClickListener {
            startActivity(Intent(this@MainActivity, PlayNext::class.java))
        }
        binding.navView.setNavigationItemSelectedListener {
            when (it.itemId) {
//                R.id.navFeedback -> startActivity(Intent(this@MainActivity, FeedbackActivity::class.java))
                R.id.navSettings -> startActivity(
                    Intent(
                        this@MainActivity,
                        SettingsActivity::class.java
                    )
                )

                R.id.navAbout -> startActivity(Intent(this@MainActivity, AboutActivity::class.java))
                R.id.navExit -> {
                    val builder = MaterialAlertDialogBuilder(this)
                    builder.setTitle("Đăng xuất")
                        .setMessage("Bạn có muốn đăng xuất khỏi ứng dụng không?")
                        .setPositiveButton("Có") { _, _ ->
                            auth.signOut()
                            startActivity(Intent(this, LoginActivity::class.java))
                        }
                        .setNegativeButton("Không") { dialog, _ ->
                            dialog.dismiss()
                        }
                    val customDialog = builder.create()
                    customDialog.show()

                    setDialogBtnBackground(this, customDialog)
                }
            }
            true
        }
    }

    //For requesting permission
    private fun requestRuntimePermission(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    13
                )
                return false
            }
        }
        //android 13 permission request
        else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.READ_MEDIA_AUDIO
                )
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.READ_MEDIA_AUDIO),
                    13
                )
                return false
            }
        }
        return true
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 13) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Quyền đã được chấp thuận", Toast.LENGTH_SHORT).show()
                initializeLayout()
            } else
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    13
                )
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item))
            return true
        return super.onOptionsItemSelected(item)
    }

    @RequiresApi(Build.VERSION_CODES.R)
    @SuppressLint("SetTextI18n")
    private fun initializeLayout() {
        search = false
        val sortEditor = getSharedPreferences("SORTING", MODE_PRIVATE)
        sortOrder = sortEditor.getInt("sortOrder", 0)
        MusicListMA = mutableListOf()
        MusicListMA = getAllAudio()
        MusicListRL = MusicListMA
        binding.musicRV.setHasFixedSize(true)
        binding.musicRV.setItemViewCacheSize(13)
        binding.musicRV.layoutManager = LinearLayoutManager(this@MainActivity)
        musicAdapter = MusicAdapter(this@MainActivity, MusicListMA)
        binding.musicRV.adapter = musicAdapter
        binding.totalSongs.text = "Total Songs : " + musicAdapter.itemCount

        //for refreshing layout on swipe from top
        binding.refreshLayout.setOnRefreshListener {
            MusicListMA = getAllAudio()
            musicAdapter.updateMusicList(MusicListMA)

            binding.refreshLayout.isRefreshing = false
        }

        // TopicLayout
        val toppicScrollView = binding.horizontalScrollView
        val tvXemThem = binding.tvXemThemChuDeTheLoai
        loadDataTopic()
        tvXemThem.setOnClickListener {
            val intent = Intent(this, AllTopicActivity::class.java)
            startActivity(intent)
        }

        // Albumlayout
        val albumList = loadAlbumData()
        val albumAdapter = AlbumAdapter(this, albumList)
        val linearLayoutAlbum = LinearLayoutManager(this)
        linearLayoutAlbum.orientation = LinearLayoutManager.HORIZONTAL
        binding.recylerViewAlbumHot.layoutManager = linearLayoutAlbum
        binding.recylerViewAlbumHot.adapter = albumAdapter
        binding.tvXemThemAlbum.visibility = View.INVISIBLE

    }

    @SuppressLint("Recycle", "Range")
    @RequiresApi(Build.VERSION_CODES.R)
    private fun getAllAudio(): MutableList<Music> {
        if (!isSongLoaded) {
            val tempList = loadSongsFromJson(this)
            for (song in tempList) {
                MusicListMA.add(song)
            }
            isSongLoaded = true
        }
        return MusicListMA
    }


    override fun onDestroy() {
        super.onDestroy()
        if (!PlayerActivity.isPlaying && PlayerActivity.musicService != null) {
            exitApplication()
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onResume() {
        super.onResume()
        loadBannerData()


        //for storing favourites data using shared preferences
        val editor = getSharedPreferences("FAVOURITES", MODE_PRIVATE).edit()
        val jsonString = GsonBuilder().create().toJson(FavouriteActivity.favouriteSongs)
        editor.putString("FavouriteSongs", jsonString)
        val jsonStringPlaylist = GsonBuilder().create().toJson(PlaylistActivity.musicPlaylist)
        editor.putString("MusicPlaylist", jsonStringPlaylist)
        editor.apply()
        //for sorting
        val sortEditor = getSharedPreferences("SORTING", MODE_PRIVATE)
        val sortValue = sortEditor.getInt("sortOrder", 0)
        if (sortOrder != sortValue) {
            sortOrder = sortValue
            MusicListMA = getAllAudio()
            musicAdapter.updateMusicList(MusicListMA)
        }
        if (PlayerActivity.musicService != null) binding.nowPlaying.visibility = View.VISIBLE

    }

    // Load BannerData
    private val handler = Handler(Looper.getMainLooper())
    private val loadBannerRunnable = object : Runnable {
        override fun run() {
            val randomIndex = (0 until MusicListRL.size).random() // Get a random index
            val song = MusicListRL[randomIndex]

            // Load data for the random song
            Glide.with(this@MainActivity)
                .load(song.artUri)
                .into(binding.imageViewBackgroundBanner)

            Glide.with(this@MainActivity)
                .load(song.artUri)
                .into(binding.imageViewBanner)

            binding.tvTitleBannerBaiHat.text = song.title
            binding.tvNoiDung.text = song.artist

            binding.relativeLayout.setOnClickListener {
                sendIntent("BannerLayout", randomIndex)
            }

            handler.postDelayed(this, 4000)
        }
    }

    private fun loadBannerData() {
        handler.removeCallbacks(loadBannerRunnable)
        handler.post(loadBannerRunnable)
    }

    private fun sendIntent(ref: String, pos: Int) {
        val intent = Intent(this, PlayerActivity::class.java)
        intent.putExtra("index", pos)
        intent.putExtra("class", ref)
        ContextCompat.startActivity(this, intent, null)
    }

    private fun loadDataTopic() {

        val imageResources = intArrayOf(
            R.drawable.vpop,
            R.drawable.usuk_logo,
            R.drawable.nhacvang,
            R.drawable.remixlogo
        )
        val linearLayout = LinearLayout(this)
        linearLayout.orientation = LinearLayout.HORIZONTAL

        for (resource in imageResources) {
            val imageView = ImageView(this)
            imageView.setImageResource(resource)
            val layoutParams = LinearLayout.LayoutParams(640, 350)
            layoutParams.gravity = Gravity.CENTER_HORIZONTAL
            imageView.layoutParams = layoutParams
            imageView.setScaleType(ImageView.ScaleType.FIT_XY)
            linearLayout.addView(imageView)
            imageView.setOnClickListener {
                when (resource) {
                    imageResources[0] -> sendTopicMusicData(
                        this,
                        AllTopicAdapter.topicVpopMusicList,
                        "Nhạc Việt Hot"
                    )

                    imageResources[1] -> sendTopicMusicData(
                        this,
                        AllTopicAdapter.topicUsUkMusicList, "Nhạc Ngoại"
                    )

                    imageResources[2] -> sendTopicMusicData(
                        this,
                        AllTopicAdapter.topicBoleroMusicList, "Nhạc trữ tình"
                    )

                    imageResources[3] -> sendTopicMusicData(
                        this,
                        AllTopicAdapter.topicRemixMusicList, "Nhạc remix"
                    )
                }
            }
        }

        binding.horizontalScrollView.addView(linearLayout)
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.search_view_menu, menu)
        //for setting gradient
        findViewById<LinearLayout>(R.id.linearLayoutNav)?.setBackgroundResource(currentGradient[themeIndex])
        val searchView = menu?.findItem(R.id.searchView)?.actionView as SearchView

        searchView.setOnQueryTextFocusChangeListener { _, hasFocus ->
            if (hasFocus){
                binding.relativeLayout.visibility = View.GONE
                binding.topicLayout.visibility = View.GONE
                binding.albumLayout.visibility = View.GONE
            }else{
                binding.relativeLayout.visibility = View.VISIBLE
                binding.topicLayout.visibility = View.VISIBLE
                binding.albumLayout.visibility = View.VISIBLE
            }
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = true
            override fun onQueryTextChange(newText: String?): Boolean {
                searching = true
                musicListSearch = ArrayList()
                if (newText != null) {
                    val userInput = newText.lowercase()
                    for (song in MusicListMA)
                        if (song.title?.lowercase()?.contains(userInput)!!)
                            musicListSearch.add(song)
                    search = true
                    musicAdapter.updateMusicList(searchList = musicListSearch)
                }
                return true
            }
        })

        return super.onCreateOptionsMenu(menu)
    }


}

//            val intent = Intent(this@MainActivity, PlayerActivity::class.java)
//            intent.putExtra("index", 0)
//            intent.putExtra("class", "MainActivity")
//            startActivity(intent)