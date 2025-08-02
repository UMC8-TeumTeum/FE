package com.example.teumteum.ui.wish.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.teumteum.data.entities.Wish
import com.example.teumteum.data.remote.wish.repository.WishRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WishGetViewModel @Inject constructor(
    private val wishRepository: WishRepository
) : ViewModel() {

    private val _wish = MutableLiveData<Wish>()
    val wish: LiveData<Wish> = _wish

    private val _getSuccess = MutableLiveData<String>()
    val getSuccess: LiveData<String> = _getSuccess

    private val _getError = MutableLiveData<String>()
    val getError: LiveData<String> = _getError

    fun getWish(wishId: Long) {
        viewModelScope.launch {
            val result = wishRepository.getWish(wishId)
            _getSuccess.value = result.toString()
            result.onFailure { e ->
                _getError.value = e.localizedMessage ?: "위시 조회에 실패했습니다."
            }
        }
    }
}