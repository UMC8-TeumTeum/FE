package com.example.teumteum.ui.wish.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.teumteum.data.remote.wish.model.DeleteWishesRequest
import com.example.teumteum.data.remote.wish.model.EditWishRequest
import com.example.teumteum.data.remote.wish.model.RegisterWishRequest
import com.example.teumteum.data.remote.wish.model.WishCategories
import com.example.teumteum.data.remote.wish.model.WishResult
import com.example.teumteum.data.remote.wish.model.WishlistItem
import com.example.teumteum.data.remote.wish.repository.WishRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WishViewModel @Inject constructor(
    private val wishRepository: WishRepository
) : ViewModel() {

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    private val _wish = MutableLiveData<WishResult>()
    val wish: LiveData<WishResult> = _wish

    private val _wishlistItems = MutableLiveData<List<WishlistItem>>()
    val wishlistItems: LiveData<List<WishlistItem>> get() = _wishlistItems

    private val _wishCategories = MutableLiveData<List<WishCategories>>()
    val wishCategories: LiveData<List<WishCategories>> = _wishCategories

    private val _registerSuccess = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val registerSuccess: SharedFlow<Unit> = _registerSuccess.asSharedFlow()

    private val _editSuccess = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val editSuccess: SharedFlow<Unit> = _editSuccess.asSharedFlow()

    private val _deleteSuccess = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val deleteSuccess: SharedFlow<Unit> = _deleteSuccess.asSharedFlow()

    // 위시 등록
    fun registerWish(request: RegisterWishRequest) {
        viewModelScope.launch {
            val result = wishRepository.registerWish(request)
            result.onSuccess {
                _registerSuccess.tryEmit(Unit)
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
                _wishlistItems.value = response.wishlist
            }.onFailure { e ->
                _errorMessage.value = e.localizedMessage ?: "위시리스트 조회에 실패했습니다."
            }
        }
    }

    // 특정 위시 조회
    fun getWish(wishId: Long) {
        viewModelScope.launch {
            val result = wishRepository.getWish(wishId)
            result.onSuccess {
                _wish.value = it
            }.onFailure { e ->
                _errorMessage.value = e.localizedMessage ?: "위시 조회에 실패했습니다."
            }
        }
    }

    // 특정 위시 편집
    fun editWish(wishId: Long, request: EditWishRequest) {
        viewModelScope.launch {
            val result = wishRepository.editWish(wishId, request)
            result.onSuccess {
                _editSuccess.tryEmit(Unit)
            }
            result.onFailure { e ->
                _errorMessage.value = e.localizedMessage ?: "위시 수정에 실패했습니다."
            }
        }
    }

    fun updateWishlistItems(updated: List<WishlistItem>) {
        _wishlistItems.value = updated
    }

    // 위시 삭제(리스트 형태)
    fun deleteWishes(request: DeleteWishesRequest) {
        viewModelScope.launch {
            val result = wishRepository.deleteWish(request)
            result.onSuccess {
                _deleteSuccess.tryEmit(Unit)
            }
            result.onFailure { e ->
                _errorMessage.value = e.localizedMessage ?: "위시 삭제에 실패했습니다."
            }
        }
    }
}