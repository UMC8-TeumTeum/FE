package com.example.teumteum.ui.activity.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.teumteum.data.remote.activity.model.ActivityWishRequest
import com.example.teumteum.data.remote.activity.model.ActivityWishResult
import com.example.teumteum.data.remote.activity.repository.ActivityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ActivityViewModel @Inject constructor(
    private val activityRepository: ActivityRepository
) : ViewModel() {

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    private val _activityWishSuccess = MutableLiveData<Boolean>()
    val activityWishSuccess: LiveData<Boolean> get() = _activityWishSuccess

    private val _activityWishes = MutableLiveData<List<ActivityWishResult>>()
    val activityWishes: LiveData<List<ActivityWishResult>> get() = _activityWishes

    // 채움활동 위시리스트 불러오기
    fun activityWish(request: ActivityWishRequest) {
        viewModelScope.launch {
            val result = activityRepository.activityWish(request)
            result.onSuccess { response ->
                _activityWishSuccess.value = true
                _activityWishes.value = response.result?.wishes
                    ?.filter { it.title.isNotBlank() }
                    .orEmpty()
            }
            result.onFailure { e ->
                _errorMessage.value = e.localizedMessage ?: "채움활동 위시 조회에 실패했습니다."
            }
        }
    }
}