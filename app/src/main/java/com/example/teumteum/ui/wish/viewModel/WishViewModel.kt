package com.example.teumteum.ui.wish.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.teumteum.data.entities.Wish
import com.example.teumteum.data.remote.wish.model.DeleteWishesRequest
import com.example.teumteum.data.remote.wish.model.EditWishRequest
import com.example.teumteum.data.remote.wish.model.RegisterWishRequest
import com.example.teumteum.data.remote.wish.model.WishlistItem
import com.example.teumteum.data.remote.wish.repository.WishRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WishViewModel @Inject constructor(
    private val wishRepository: WishRepository
) : ViewModel() {

    private val _successMessage = MutableLiveData<String>()
    val successMessage: LiveData<String> = _successMessage

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    private val _wish = MutableLiveData<Wish>()
    val wish: LiveData<Wish> = _wish

    val wishlistItems = MutableLiveData<List<WishlistItem>>()

    // 위시 등록
    fun registerWish(request: RegisterWishRequest) {
        viewModelScope.launch {
            val result = wishRepository.registerWish(request)
            result.onSuccess {
                _successMessage.value = "위시가 등록되었습니다."
            }
            result.onFailure { e ->
                _errorMessage.value = e.localizedMessage ?: "위시 등록에 실패했습니다."
            }
        }
    }

    // 위시리스트 조회
    fun getWishlist(duration: String, page: Int) {
        viewModelScope.launch {
            val result = wishRepository.getWishlist(duration, page)

            result.onSuccess { response ->
                wishlistItems.value = response.wishlist
                _successMessage.value = "위시리스트가 성공적으로 조회되었습니다."
            }.onFailure { e ->
                _errorMessage.value = e.localizedMessage ?: "위시리스트 조회에 실패했습니다."
            }
        }
    }

    // 특정 위시 조회
    fun getWish(wishId: Long) {
        viewModelScope.launch {
            val result = wishRepository.getWish(wishId)

            result.onSuccess { response ->
                _wish.value = response
                _successMessage.value = "위시가 성공적으로 조회되었습니다."
            }.onFailure { e ->
                _errorMessage.value = e.localizedMessage ?: "위시 조회에 실패했습니다."
            }
        }
    }

    // 특정 위시 편집
    fun editWish(wishId: Long, request: EditWishRequest) {
        viewModelScope.launch {
            val result = wishRepository.editWish(wishId, request)
            _successMessage.value = result.toString()
            result.onSuccess {
                _successMessage.value = "위시가 성공적으로 수정되었습니다."
            }.onFailure { e ->
                _errorMessage.value = e.localizedMessage ?: "위시 수정에 실패했습니다."
            }
        }
    }

    // 위시 삭제(리스트 형태)
    fun deleteWishes(request: DeleteWishesRequest) {
        viewModelScope.launch {
            val result = wishRepository.deleteWish(request)
            _successMessage.value = result.toString()
            result.onSuccess {
                _successMessage.value = "위시가 성공적으로 삭제되었습니다."
            }.onFailure { e ->
                _errorMessage.value = e.localizedMessage ?: "위시 삭제에 실패했습니다."
            }
        }
    }
}