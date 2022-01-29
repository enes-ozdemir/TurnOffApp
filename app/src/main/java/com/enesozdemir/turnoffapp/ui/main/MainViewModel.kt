package com.enesozdemir.turnoffapp.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.enesozdemir.turnoffapp.AwarenessTimeInfo

class MainViewModel : ViewModel() {

    private val _getTimeCategoryId = MutableLiveData<Int>()
    val getTimeCategoryId: LiveData<Int>
        get() = _getTimeCategoryId
    fun getAwarenessTimeCategoryInfo(awarenessTimeInfo: AwarenessTimeInfo) {
        awarenessTimeInfo.let {
            _getTimeCategoryId.postValue(awarenessTimeInfo.timeCategory)
        }
    }
}