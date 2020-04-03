package cn.imgrocket.imgrocket

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.room.Room
import androidx.viewpager.widget.ViewPager
import cn.imgrocket.imgrocket.adapter.SimplePageFragmentAdapter
import cn.imgrocket.imgrocket.databinding.ActivityMainBinding
import cn.imgrocket.imgrocket.room.AppDatabase
import cn.imgrocket.imgrocket.room.model.User
import cn.imgrocket.imgrocket.tool.APP
import cn.imgrocket.imgrocket.tool.AvatarListener
import cn.imgrocket.imgrocket.tool.Function.black
import cn.imgrocket.imgrocket.tool.URL
import cn.imgrocket.imgrocket.tool.UserStateChangeListener
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import java.io.File


class MainActivity : AppCompatActivity(), AvatarListener, UserStateChangeListener {
    private lateinit var adapter: SimplePageFragmentAdapter
    private lateinit var binding: ActivityMainBinding
    private val global: APP by lazy { application as APP }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        when (intent.extras?.get("Function")) {
            1 -> {
                val intent = Intent()
                intent.setClass(this, ChooseMultiPhotoActivity::class.java)
                intent.putExtra("Function", 1)
                startActivity(intent)
            }
            2 -> {
                val intent = Intent()
                intent.setClass(this, ChooseSinglePhotoActivity::class.java)
                intent.putExtra("Function", 2)
                startActivity(intent)
            }
            3 -> {
                val intent = Intent()
                intent.setClass(this, ChooseMultiPhotoActivity::class.java)
                intent.putExtra("Function", 3)
                startActivity(intent)
            }
        }
        binding = ActivityMainBinding.inflate(layoutInflater)

        global.database = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "database").allowMainThreadQueries().build()
        global.userDao = global.database.userDao()
        global.user = global.userDao.loadUser()
        global.userData = global.userDao.autoUpdateLoadUser()
        global.userData.observe(this, Observer {
            global.user = it
        })

        global.addAvatarChangeListener(this)
        global.addOnUserStateChangeListener(this)

        onAvatarChange(0)
        setContentView(binding.root)

        black(this)
        init()

        if (needHelp()) {
            val intent = Intent()
            intent.setClass(this, HelpActivity::class.java)
            startActivity(intent)
        }

        binding.mainImgHelp.setOnClickListener {
            val intent = Intent()
            intent.setClass(this, HelpActivity::class.java)
            startActivity(intent)
        }
        binding.mainImgAvatar.setOnClickListener {
            val intent = Intent()
            if (global.user != null) {
                intent.setClass(this, UploadAvatarActivity::class.java)
            } else {
                intent.setClass(this, LoginActivity::class.java)
            }
            startActivity(intent)
        }

        binding.mainNavNav.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.menu_processing -> {
//                    binding.mainImgAvatar.visibility = View.VISIBLE
                    binding.mainImgAvatar.fadeInAndShow()
                    changeSmallAvatar()
                    binding.mainPageView.currentItem = 0
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.menu_done -> {
//                    binding.mainImgAvatar.visibility = View.VISIBLE
                    binding.mainImgAvatar.fadeInAndShow()
                    changeSmallAvatar()
                    binding.mainPageView.currentItem = 1
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.menu_my -> {
//                    binding.mainImgAvatar.visibility = View.INVISIBLE
                    binding.mainImgAvatar.fadeOutAndHide()
                    binding.mainPageView.currentItem = 2
                    return@setOnNavigationItemSelectedListener true
                }
            }
            false

        }
        binding.mainPageView.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {}

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

            override fun onPageSelected(position: Int) {
                binding.mainNavNav.selectedItemId = binding.mainNavNav.menu.getItem(position).itemId
            }
        })
    }

    private fun View.fadeOutAndHide() {
        val before = if (this@fadeOutAndHide.visibility == View.GONE) 0f else 1f
        val fadeOut: Animation = AlphaAnimation(before, 0f)
        fadeOut.interpolator = AccelerateInterpolator()
        fadeOut.duration = 200
        fadeOut.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationEnd(animation: Animation?) {
                this@fadeOutAndHide.visibility = View.GONE
            }
            override fun onAnimationRepeat(animation: Animation?) {}
            override fun onAnimationStart(animation: Animation?) {}
        })
        this@fadeOutAndHide.startAnimation(fadeOut)
    }

    private fun View.fadeInAndShow() {
        val before = if (this@fadeInAndShow.visibility == View.VISIBLE) 1f else 0f
        val fadeIn: Animation = AlphaAnimation(before, 1f)
        fadeIn.interpolator = AccelerateInterpolator()
        fadeIn.duration = 200
        fadeIn.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationEnd(animation: Animation?) {
                this@fadeInAndShow.visibility = View.VISIBLE
            }
            override fun onAnimationRepeat(animation: Animation?) {}
            override fun onAnimationStart(animation: Animation?) {}
        })
        this@fadeInAndShow.startAnimation(fadeIn)
    }

    private fun init() {
        adapter = SimplePageFragmentAdapter(supportFragmentManager, arrayListOf(ProcessingFragment(), DoneFragment(), UserFragment()))
        binding.mainPageView.adapter = adapter
        binding.mainPageView.currentItem = 0
    }

    private fun needHelp(): Boolean {
        val f = File(applicationContext.filesDir.path + "/notNew")
        return !f.exists()
    }

    override fun onAvatarChange(avatarVersion: Int) {
        changeSmallAvatar()
    }

    override fun onUserChange(user: User?) {
        changeSmallAvatar()
    }

    override fun onDestroy() {
        super.onDestroy()
        global.removeAvatarChangeListener(this)
        global.removeOnUserStateChangeListener(this)
    }

    private fun changeSmallAvatar() {
        global.user?.apply {
            Glide.with(this@MainActivity)
                    .load(URL.avatarURL + uid + "&version=" + global.avatarVersion)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(binding.mainImgAvatar)
            binding.mainImgAvatar.setColorFilter(Color.TRANSPARENT)
        }
    }
}