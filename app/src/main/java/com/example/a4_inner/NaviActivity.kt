package com.example.a4_inner

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.a4_inner.databinding.ActivityNaviBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.firestore


private const val TAG_HOME = "home_fragment"
private const val TAG_BULLETIN = "bulletin_fragment"
private const val TAG_TIMETABLE = "timetable_fragment"
private const val TAG_MAP = "map_fragment"
private const val TAG_AR = "ar_fragment"
private const val TAG_TODAY_CLASS = "today_class_fragment"
private const val TAG_RECENT_DEST = "recent_dest_fragment"

class NaviActivity : AppCompatActivity() {

    public lateinit var binding: ActivityNaviBinding

    // variable for GOOGLE login
//    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNaviBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setFragment(TAG_HOME, HomeFragment())

        binding.cameraBtn.setOnClickListener{
            val packageName = "com.google.android.apps.translate"
            val intent = packageManager.getLaunchIntentForPackage(packageName)
            if (intent != null) {
                // 앱이 설치되어 있으면 앱 실행
                startActivity(intent)
            } else {
                // 앱이 설치되어 있지 않으면 Play Store에서 앱 페이지로 이동
                val marketUri = Uri.parse("market://details?id=$packageName")
                val marketIntent = Intent(Intent.ACTION_VIEW, marketUri)
                startActivity(marketIntent)
            }
        }

        binding.myPageBtn.setOnClickListener {
            val intent = Intent(this, MyPageActivity::class.java)
            startActivity(intent)
        }

        binding.navigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> {
                    setFragment(TAG_HOME, HomeFragment())
                    val fragment = supportFragmentManager.findFragmentByTag(TAG_HOME)
                    if (fragment is HomeFragment) {
                        fragment.refresh()
                    }
                }
                R.id.bulletin -> setFragment(TAG_BULLETIN, BulletinFragment())
                R.id.timetable -> setFragment(TAG_TIMETABLE, TimetableFragment())
                R.id.map -> setFragment(TAG_MAP, MapFragment())
                R.id.ar -> setFragment(TAG_AR, ArFragment())
            }
            true
        }

        // User verification
        // 만약 로그인 액티비티에서 넘어왔다면
        if (intent.getStringExtra("fromLogin") == "true") {
            val userAuth = Firebase.auth.currentUser
            if (userAuth != null) {
                Toast.makeText(this, "Welcome, ${CurrentUser.getName}!", Toast.LENGTH_SHORT).show()
                intent.removeExtra("fromLogin")
                Log.d("ITM","PhotoUrl: ${CurrentUser.getPhotoUrl}")
            } else {
                // If user tries to access Navi Activity with no auth, directly navigate to LogInActivity
                // Create an Intent to start the LoginActivity
                val intent = Intent(this, LogInActivity::class.java)

                Log.d("ITM","Fuck OFF")
                Log.d("ITM", "fromLogin: ${intent.getStringExtra("fromLogin")}")
                Log.d("ITM", "Firebase.auth.currentUser: ${Firebase.auth.currentUser}")


                // Optional: Add any extra information to the intent
                // intent.putExtra("key", "value")

                // Start the LoginActivity
                startActivity(intent)
                finish()
            }
        }
        // 수호 로그인일때 허용.
        else if (intent.getStringExtra("fromLogin") == "suho"){
            intent.removeExtra("fromLogin")
            Log.d("ITM","Current userUid = ${CurrentUser.getUserUid}")
        }
    }

    public fun setFragment(tag:String, fragment: Fragment) {
        val manager: FragmentManager = supportFragmentManager
        val fragTransaction = manager.beginTransaction()

        if (manager.findFragmentByTag(tag) == null) {
            fragTransaction.add(R.id.mainFrameLayout, fragment, tag)
        }

        val home = manager.findFragmentByTag(TAG_HOME)
        val bulletin = manager.findFragmentByTag(TAG_BULLETIN)
        val timetable = manager.findFragmentByTag(TAG_TIMETABLE)
        val map = manager.findFragmentByTag(TAG_MAP)
        val ar = manager.findFragmentByTag(TAG_AR)

        val navigationView = findViewById<BottomNavigationView>(R.id.navigationView)
        if (home != null) {
            fragTransaction.hide(home)
        }

        if (bulletin != null) {
            fragTransaction.hide(bulletin)
        }

        if (timetable != null) {
            fragTransaction.hide(timetable)
        }

        if (map != null) {
            fragTransaction.hide(map)
        }

        if (ar != null) {
            fragTransaction.hide(ar)
        }

        if (tag == TAG_HOME) {
            if (home != null) {
                fragTransaction.show(home)
                navigationView.menu[0].setChecked(true)

            }
        } else if (tag == TAG_BULLETIN) {
            if (bulletin != null) {
                fragTransaction.show(bulletin)
                navigationView.menu[1].setChecked(true)
            }
        } else if (tag == TAG_TIMETABLE) {
            if (timetable != null) {
                fragTransaction.show(timetable)
                navigationView.menu[2].setChecked(true)
            }
        } else if (tag == TAG_MAP) {
            if (map != null) {
                fragTransaction.show(map)
                navigationView.menu[3].setChecked(true)
            }
        } else if (tag == TAG_AR) {
            if (ar != null) {
                fragTransaction.show(ar)
                navigationView.menu[4].setChecked(true)
            }
        }
        fragTransaction.commitAllowingStateLoss()

    }
}

