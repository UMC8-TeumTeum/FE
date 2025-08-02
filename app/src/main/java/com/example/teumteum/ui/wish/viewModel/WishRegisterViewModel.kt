package com.example.teumteum.ui.wish.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.teumteum.data.remote.wish.model.RegisterWishRequest
import com.example.teumteum.data.remote.wish.repository.WishRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WishRegisterViewModel @Inject constructor(
    private val wishRepository: WishRepository
) : ViewModel() {

    private val _registerSuccess = MutableLiveData<String>()
    val registerSuccess: LiveData<String> = _registerSuccess

    private val _registerError = MutableLiveData<String>()
    val registerError: LiveData<String> = _registerError

    fun registerWish(request: RegisterWishRequest) {
        viewModelScope.launch {
            val result = wishRepository.registerWish(request)
            result.onSuccess {
                _registerSuccess.value = "위시가 등록되었습니다."
            }
            result.onFailure { e ->
                _registerError.value = e.localizedMessage ?: "위시 등록에 실패했습니다."
            }
        }
    }
}