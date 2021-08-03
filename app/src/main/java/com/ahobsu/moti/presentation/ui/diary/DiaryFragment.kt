package com.ahobsu.moti.presentation.ui.diary

import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ahobsu.moti.R
import com.ahobsu.moti.data.injection.Injection
import com.ahobsu.moti.databinding.FragmentDiaryBinding
import com.ahobsu.moti.presentation.BaseFragment
import com.ahobsu.moti.presentation.ui.diary.adapter.DiaryAdapter
import com.ahobsu.moti.presentation.ui.main.MainActivity
import java.text.SimpleDateFormat
import java.util.*

class DiaryFragment :
    BaseFragment<FragmentDiaryBinding>(R.layout.fragment_diary) {

    private var listSize = 0
    private var isRenewable = true
    private var isRenewableTop = true
    private var isRenewableBottom = true

    private val viewModel by lazy {
        ViewModelProvider(
            viewModelStore,
            DiaryViewModelFactory(Injection.provideAnswerRepository())
        ).get(DiaryViewModel::class.java)
    }

    private val diaryAdapter by lazy {
        DiaryAdapter().apply {
            setOnItemClickListener(object : DiaryAdapter.OnItemClickListener {
                override fun onItemClick(id: Int) {
                    //TODO("Not yet implemented")
                }
            })
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.viewModel = viewModel
        initRecyclerView()
        viewModel.initAnswersDays()

        val date = Calendar.getInstance()
        val datetime = SimpleDateFormat("yyyy.MM.DD", Locale.KOREA).format(date.time)
        onChangeCalenderDate(datetime, isToday = true)

        binding.diaryRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                Log.e("test2", "isRenewable $isRenewable")
                Log.e("test2", "isRenewableTop $isRenewableTop")
                Log.e("test2", "isRenewableBottom $isRenewableBottom")

                if (isRenewable) {
                    val lastVisibleItemPosition =
                        (recyclerView.layoutManager as LinearLayoutManager).findLastVisibleItemPosition()
                    Log.e("test2", "lastVisibleItemPosition: $lastVisibleItemPosition")

                    if (isRenewableBottom && lastVisibleItemPosition + 1 == listSize) {
                        Log.e("test2", "바답ㄱ")
                        viewModel.onScrollEvent(false)
                        isRenewable = false

                    }
                    if (isRenewableTop && lastVisibleItemPosition <= 3) {
                        Log.e("test2", "천장dd")
                        viewModel.onScrollEvent(true)
                        isRenewable = false
                    }
                }
            }
        })

        viewModel.diaryList.observe(viewLifecycleOwner) {
            diaryAdapter.submitList(it)
            listSize = it.size
            isRenewable = true

            Log.e("listSize", " ::: ?  $listSize")
        }
        viewModel.selectedCalender.observe(viewLifecycleOwner) {
            (activity as MainActivity).addSelectedCalenderFragment()
        }
        viewModel.writeDayList.observe(viewLifecycleOwner) {
            diaryAdapter.setWriteDayList(it)
        }

        viewModel.isRenewableTop.observe(viewLifecycleOwner) {
            Log.e("top isRenewableTop", " $isRenewableTop")
            isRenewableTop = it
            isRenewable = true
        }
        viewModel.isRenewableBottom.observe(viewLifecycleOwner) {
            isRenewableBottom = it
            isRenewable = true
            val deco = SpaceDecoration(200)
            binding.diaryRecyclerView.addItemDecoration(deco)
            Log.e("바닥 isRenewableBottom", " $isRenewableBottom")

        }
    }

    inner class SpaceDecoration(private val size: Int) : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            Log.e("outRect.bottom1 "," "+outRect.bottom )

            super.getItemOffsets(outRect, view, parent, state)
            if (parent.getChildAdapterPosition(view) == listSize - 1) {
                outRect.bottom += size
                Log.e("outRect.bottom2 "," "+outRect.bottom )

            } else {
                outRect.bottom  = 0
            }
        }
    }

    fun onChangeCalenderDate(date: String, isToday: Boolean) {
        viewModel.setDate(date, isToday)
    }

    private fun initRecyclerView() {
        binding.diaryRecyclerView.apply {
            adapter = diaryAdapter
        }
    }

    companion object {
        fun newInstance() = DiaryFragment()
    }
}