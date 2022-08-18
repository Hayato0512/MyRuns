package com.example.myruns2

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class MainActivity : AppCompatActivity() {
    private lateinit var fragmentStart:FragmentStart
    private lateinit var fragmentHistory:FragmentHistory
    private lateinit var fragmentSetting: FragmentSetting
    private lateinit var fragments:ArrayList<Fragment>
    private lateinit var tab:TabLayout
    private lateinit var viewPager: ViewPager2
    private lateinit var myFragmentStateAdapter:FragmentStateAdapter
    private lateinit var tabLayoutMediator: TabLayoutMediator
    private lateinit var tabConfigurationStrategy: TabLayoutMediator.TabConfigurationStrategy
    private val TAB_TEXT = arrayOf("Start", "History", "Setting")
    private lateinit var  fragmentTransaction:FragmentTransaction
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

       fragmentStart = FragmentStart()
        fragmentHistory = FragmentHistory()
        fragmentSetting = FragmentSetting()

        fragments = ArrayList()
        fragments.add(fragmentStart)
        fragments.add(fragmentHistory)
        fragments.add(fragmentSetting)


        tab = findViewById(R.id.tab)
        viewPager = findViewById(R.id.viewpager)
        myFragmentStateAdapter = MyFragmentsStateAdapter(this, fragments)
        viewPager.adapter = myFragmentStateAdapter



        tabConfigurationStrategy =TabLayoutMediator.TabConfigurationStrategy(){
            tab:TabLayout.Tab, position:Int ->
            tab.text = TAB_TEXT[position]
        }
        tabLayoutMediator   = TabLayoutMediator(tab, viewPager, tabConfigurationStrategy)
        tabLayoutMediator.attach()



    }
}