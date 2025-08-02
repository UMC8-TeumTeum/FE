package com.example.teumteum.ui.wish.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.teumteum.data.remote.wish.model.DeleteWishesRequest
import com.example.teumteum.data.remote.wish.repository.WishRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WishDeleteViewModel @Inject constructor(
    private val wishRepository: WishRepository
) : ViewModel() {

    private val _deleteSuccess = MutableLiveData<String>()
    val deleteSuccess: LiveData<String> = _deleteSuccess

    private val _deleteError = MutableLiveData<String>()
    val deleteError: LiveData<String> = _deleteError

    fun deleteWishes(request: DeleteWishesRequest) {
        viewModelScope.launch {
            val result = wishRepository.deleteWish(request)
            _deleteSuccess.value = result.toString()
            result.onFailure { e ->
                _deleteError.value = e.localizedMessage ?: "위시 등록에 실패했습니다."
            }
        }
    }
}