package com.dongdong.neonplayer.viewpager

import android.util.Log
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.viewpager2.adapter.FragmentStateAdapter
import kotlinx.android.synthetic.main.appbar_musiclist.*
import javax.annotation.Nonnull

class MusicList_ViewPagerAdapter  : FragmentStateAdapter {

    var mpagenum : Int
    var mfragment : Fragment

    constructor(fragment: Fragment, pagenumber: Int) : super(fragment){
        this.mfragment = fragment
        this.mpagenum = pagenumber
    }

    @Nonnull
    override fun createFragment(position: Int): Fragment {
        var index = getPosition(position)
        Log.d("qwer123456", "인덱스값: ${index} // $position")
        if(index == 0) {
            return ViewPager24HitsList.newInstance(index)
        }else if(index == 1) {
            return ViewPagerNeonChartList.newInstance(index)
        }else if(index == 2) {
            return ViewPagerPOPChartList.newInstance(index)
        }else {
            return ViewPager24HitsList.newInstance(index)
        }
    }

    fun getPosition(position : Int) : Int{
        return position % mpagenum
    }

    override fun getItemId(position: Int): Long {
        return super.getItemId(position)
    }

    override fun getItemCount(): Int {
        return mpagenum
    }

}