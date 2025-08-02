package com.example.teumteum.ui.wish.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.teumteum.data.remote.wish.model.EditWishRequest
import com.example.teumteum.data.remote.wish.repository.WishRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WishEditViewModel @Inject constructor(
    private val wishRepository: WishRepository
) : ViewModel() {

    private val _editSuccess = MutableLiveData<String>()
    val editSuccess: LiveData<String> = _editSuccess

    private val _editError = MutableLiveData<String>()
    val editError: LiveData<String> = _editError

    fun editWish(wishId: Long, request: EditWishRequest) {
        viewModelScope.launch {
            val result = wishRepository.editWish(wishId, request)
            _editSuccess.value = result.toString()
            result.onFailure { e ->
                _editError.value = e.localizedMessage ?: "위시 등록에 실패했습니다."
            }
        }
    }
}