package com.example.teumteum.ui.wish.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.teumteum.data.remote.wish.model.WishlistItem
import com.example.teumteum.data.remote.wish.repository.WishRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WishlistGetViewModel @Inject constructor(
    private val wishRepository: WishRepository
) : ViewModel() {

    val wishlistItems = MutableLiveData<List<WishlistItem>>()

    private val _getSuccess = MutableLiveData<String>()
    val getSuccess: LiveData<String> = _getSuccess

    private val _getError = MutableLiveData<String>()
    val getError: LiveData<String> = _getError

    fun getWishlist(duration: String, page: Int) {
        viewModelScope.launch {
            val result = wishRepository.getWishlist(duration, page)

            result.onSuccess { response ->
                wishlistItems.value = response.wishlist
            }.onFailure { e ->
                _getError.value = e.localizedMessage ?: "위시리스트 조회에 실패했습니다."
            }
        }
    }
}