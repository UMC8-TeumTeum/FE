package com.example.teumteum.ui.alarm.viewModel


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.teumteum.data.remote.alarm.dto.NotificationResponse
import com.example.teumteum.data.remote.alarm.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val repo: NotificationRepository
) : ViewModel() {

    private val _items = MutableLiveData<List<NotificationResponse>>(emptyList())
    val items: LiveData<List<NotificationResponse>> = _items

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    // paging states
    private var page = 1
    private val size = 10
    private var hasNext = true
    private var isLoading = false
    private var runningJob: Job? = null

    fun loadFirst() {
        if (isLoading) return
        page = 1
        hasNext = true
        _items.value = emptyList()
        fetch(page)
    }

    fun loadNext() {
        if (isLoading || !hasNext) return
        fetch(page + 1)
    }

    private fun fetch(targetPage: Int) {
        isLoading = true
        runningJob?.cancel()
        runningJob = viewModelScope.launch {
            repo.getNotifications(targetPage, size)
                .onSuccess { pageData ->
                    hasNext = pageData.hasNext
                    page = pageData.currentPage

                    val merged = if (targetPage == 1) {
                        pageData.content
                    } else {
                        val old = _items.value.orEmpty()
                        old + pageData.content
                    }
                    _items.value = merged
                }
                .onFailure { e ->
                    _error.value = e.message
                }
            isLoading = false
        }
    }
}