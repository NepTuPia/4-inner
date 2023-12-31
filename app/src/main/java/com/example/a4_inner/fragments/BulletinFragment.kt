package com.example.a4_inner.fragments

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.a4_inner.Board
import com.example.a4_inner.FireBase
import com.example.a4_inner.FragmentTags
import com.example.a4_inner.activities.NaviActivity
import com.example.a4_inner.post.Post
import com.example.a4_inner.post.Posting
import com.example.a4_inner.R
import com.example.a4_inner.RecyclerUserAdapter
import com.example.a4_inner.databinding.FragmentBulletinBinding
import com.example.a4_inner.fetchRecentBulletinData
import com.google.firebase.firestore.Query
import kotlinx.coroutines.launch
import kotlinx.coroutines.MainScope

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
/**
 * A simple [Fragment] subclass.
 * Use the [BulletinFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class BulletinFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var binding: FragmentBulletinBinding
    private lateinit var adapter: RecyclerUserAdapter
    private var list = ArrayList<Board>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentBulletinBinding.inflate(inflater, container, false)
        fetchDataFromFirestore()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val recyclerView: RecyclerView = binding.lstUser

        adapter = RecyclerUserAdapter(list)

        adapter.setOnItemClickListener(object : RecyclerUserAdapter.OnItemClickListener {
            override fun onItemClick(data: Board, pos: Int) {
                val intent = Intent(requireContext(), Posting::class.java)
                intent.putExtra("Title", data.title)
                intent.putExtra("Contents", data.contents)
                intent.putExtra("PostId", data.documentId)  // Document Id를 Intent에 추가
                startActivity(intent)
            }
        })

        binding.postBtn.setOnClickListener {
            val intent = Intent(requireContext(), Post::class.java)
            startActivityForResult(intent, POST_ACTIVITY_REQUEST_CODE)
        }

        recyclerView.adapter = adapter
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            val boardData = data?.getSerializableExtra("boardData") as? Board

            if (boardData != null) {
                val existingBoard = list.find { it.title == boardData.title && it.contents == boardData.contents }

                if (existingBoard != null) {
                    // Update the existing board in the list
                    existingBoard.title = boardData.title
                    existingBoard.contents = boardData.contents
                    adapter.notifyDataSetChanged()
                } else {
                    // Add the new boardData to the list
                    addNewItem(boardData)
                }
            }
        } else {
            Log.d("ITM", "Invalid requestCode or resultCode: requestCode=$requestCode, resultCode=$resultCode")
        }
    }
    fun fetchDataFromFirestore() {
        MainScope().launch {
            FireBase.db.collection("Board")
                .orderBy("creationTime", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, exception ->
                    if (exception != null) {
                        Log.e("ITM", "Error getting documents: ", exception)
                        return@addSnapshotListener
                    }

                    if (snapshot != null) {
                        val newDataList = ArrayList<Board>()

                        for (document in snapshot) {
                            val title = document.getString("title")
                            val contents = document.getString("contents")
                            val user_id = document.getString("user_id")

                            if (!title.isNullOrBlank() && !contents.isNullOrBlank() && !user_id.isNullOrBlank()) {
                                newDataList.add(
                                    Board(
                                        img = R.drawable.user,
                                        title = title!!,
                                        contents = contents!!,
                                        user_id = user_id!!,
                                        documentId = document.id
                                    )
                                )
                            }
                        }
                        list.clear()
                        list.addAll(newDataList)
                        adapter.notifyDataSetChanged()
                        val home_fragment = (requireActivity() as? NaviActivity)?.supportFragmentManager?.findFragmentByTag(
                            FragmentTags.TAG_HOME
                        ) as? HomeFragment
                        fetchRecentBulletinData(home_fragment)
                    } else {
                        Log.d("ITM", "Current data: null")
                    }
                }
        }
    }


    fun addNewItem(item: Board) {
        list.add(0, item)
        adapter.notifyItemInserted(0)
    }

    fun getAdapter(): RecyclerUserAdapter {
        return adapter
    }

    companion object {
        const val POST_ACTIVITY_REQUEST_CODE = 1

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment BulletinFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            BulletinFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}