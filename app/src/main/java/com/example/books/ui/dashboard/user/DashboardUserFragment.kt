package com.example.books.ui.dashboard.user

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.navigation.fragment.findNavController
import androidx.viewpager.widget.ViewPager
import com.example.books.R
import com.example.books.data.ModelCategory
import com.example.books.databinding.FragmentDashboardUserBinding
import com.example.books.ui.dashboard.user.viewpager.BooksUserFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class DashboardUserFragment : Fragment(R.layout.fragment_dashboard_user) {

    //view binding
    private lateinit var binding: FragmentDashboardUserBinding

    //firebase auth
    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var categoryList: ArrayList<ModelCategory>

    private lateinit var viewPagerAdapter: ViewPagerAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentDashboardUserBinding.bind(view)

        //init firebase auth
        firebaseAuth = FirebaseAuth.getInstance()
        checkUser()

        setupWithViewPagerAdapter(binding.viewPager2)
        binding.tableLayout.setupWithViewPager(binding.viewPager2)

        //handle click, logout
        binding.logout.setOnClickListener {
            firebaseAuth.signOut()
            findNavController().navigate(R.id.action_dashboardUserFragment_to_welcomeFragment)
        }

        //handle click, open Profile activity
        binding.btnProfile.setOnClickListener {
            findNavController().navigate(R.id.action_dashboardUserFragment_to_profileFragment)
        }
    }

    private fun setupWithViewPagerAdapter(viewPager: ViewPager){
        viewPagerAdapter = ViewPagerAdapter(
            requireActivity().supportFragmentManager,
            FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT,
            requireContext()
        )

        //init list
        categoryList = arrayListOf()

        //load categories from db
        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                //clear list
                categoryList.clear()

                /*Load some static categories e.g All, Most Viewed, Most Downloaded*/
                //add data to models
                val modelAll = ModelCategory("01", "All", 1, "")
                val modelMostViewed = ModelCategory("01", "Most Viewed", 1, "")
                val modelMostDownloaded = ModelCategory("01", "Most Downloaded", 1, "")

                //add to list
                categoryList.add(modelAll)
                categoryList.add(modelMostViewed)
                categoryList.add(modelMostDownloaded)
                //add to viewPagerAdapter
                viewPagerAdapter.addFragment(
                    BooksUserFragment.newIstance(modelAll.id,modelAll.category, modelAll.uid),modelAll.category)
                viewPagerAdapter.addFragment(
                    BooksUserFragment.newIstance(modelMostViewed.id,modelMostViewed.category, modelMostViewed.uid),modelMostViewed.category)
                viewPagerAdapter.addFragment(
                    BooksUserFragment.newIstance(modelMostDownloaded.id,modelMostDownloaded.category, modelMostDownloaded.uid),modelMostDownloaded.category)

                //refresh list
                viewPagerAdapter.notifyDataSetChanged()

                //Now load from firebase db
                for (ds in snapshot.children){
                    //get data in model
                    val model = ds.getValue(ModelCategory::class.java)
                    //add to list
                    categoryList.add(modelAll)
                    //add to viewPagerAdapter
                    if (model != null) {
                        viewPagerAdapter.addFragment(
                            BooksUserFragment.newIstance("${model.id}",model.category, model.uid),model.category)
                    }
                    //refresh list
                    viewPagerAdapter.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })

        //setup adapter to viewpager
        viewPager.adapter = viewPagerAdapter

    }

    class ViewPagerAdapter(fm: FragmentManager,behavior:Int,context: Context): FragmentPagerAdapter(fm, behavior) {
        //holds list of fragments i.e new instances of same fragment for each category
        private val fragmentList = arrayListOf<BooksUserFragment>()
        //list of titles of categories, for tabs
        private val fragmentTitleList = arrayListOf<String>()

        private val context = context

        override fun getCount(): Int = fragmentList.size

        override fun getItem(position: Int): Fragment {
            return fragmentList[position]
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return fragmentTitleList[position]
        }

        fun addFragment(fragment: BooksUserFragment, title: String){
            //add fragment that will be passed as parameter in fragmentList
            fragmentList.add(fragment)
            //add title that will be passed as parameter
            fragmentTitleList.add(title)
        }

    }

    //this activity can be opened with or without login, so hide logout and profile button when user not logged in
    private fun checkUser() {
        //get current user
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser == null){
            //not logged in, user can stay in user dashboard without login too
            binding.userTv.text = "Not Logged In"

            //hide profile, logout
            binding.btnProfile.visibility = View.GONE
            binding.logout.visibility = View.GONE
        } else {
            //logged in, get and show user info
            val email = firebaseUser.email
            binding.userTv.text = email

            //show profile, logout
            binding.btnProfile.visibility = View.VISIBLE
            binding.logout.visibility = View.VISIBLE

        }
    }
}