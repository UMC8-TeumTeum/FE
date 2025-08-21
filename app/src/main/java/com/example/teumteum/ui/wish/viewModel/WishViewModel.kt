package com.example.teumteum.ui.wish.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.teumteum.data.remote.activity.model.AssignWishRequest
import com.example.teumteum.data.remote.wish.model.DeleteWishesRequest
import com.example.teumteum.data.remote.wish.model.EditWishRequest
import com.example.teumteum.data.remote.wish.model.RegisterWishRequest
import com.example.teumteum.data.remote.wish.model.WishCategories
import com.example.teumteum.data.remote.wish.model.WishResult
import com.example.teumteum.data.remote.wish.model.WishlistItem
import com.example.teumteum.data.remote.wish.repository.WishRepository
import com.example.teumteum.utils.ApiException
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

    private val _errorState = MutableLiveData<ApiException>()
    val errorState: LiveData<ApiException> = _errorState

    private val _errorCode = MutableLiveData<String?>()
    val errorCode: LiveData<String?> = _errorCode

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

    private val _assignSuccess = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val assignSuccess: SharedFlow<Unit> = _assignSuccess.asSharedFlow()

    private val _assignError = MutableSharedFlow<ApiException>(replay = 0, extraBufferCapacity = 1)
    val assignError: SharedFlow<ApiException> = _assignError.asSharedFlow()

    // 페이징 상태
    private var currentPage = 1
    private var currentDuration: String = "all"
    private var hasNext: Boolean = true
    private var loadingInProgress = false

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

    // 위시리스트 조회 (refresh)
    fun refreshWishlist(duration: String) {
        currentDuration = duration
        currentPage = 1
        hasNext = true
        loadWishlist(reset = true)
    }

    // 현재 duration으로 1페이지부터 다시 불러오기
    fun refreshCurrent() {
        refreshWishlist(currentDuration)
    }

    // 다음 페이지 조회
    fun loadNextPage() {
        if (!hasNext || loadingInProgress) return
        currentPage += 1
        loadWishlist(reset = false, onFail = {
            currentPage = maxOf(1, currentPage - 1)
        })
    }


    private fun loadWishlist(reset: Boolean, onFail: (() -> Unit)? = null) {
        viewModelScope.launch {
            try {
                loadingInProgress = true
                val result = wishRepository.getWishlist(currentDuration, currentPage)
                result.onSuccess { response ->
                    hasNext = response.hasNext == true
                    val incoming = response.wishlist.orEmpty()
                    _wishlistItems.value = if (reset) incoming else _wishlistItems.value.orEmpty() + incoming
                }.onFailure { e ->
                    onFail?.invoke()
                    _errorMessage.value = e.localizedMessage ?: "위시리스트 조회에 실패했습니다."
                }
            } catch (t: Throwable) {
                onFail?.invoke()
                _errorMessage.value = t.localizedMessage ?: "위시리스트 조회 중 오류가 발생했습니다."
            } finally {
                loadingInProgress = false
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

    // 위시 빈틈 채우기
    fun assignWish(wishId: Long, request: AssignWishRequest) {
        viewModelScope.launch {
            val result = wishRepository.assignWish(wishId, request)
            result.onSuccess {
                _assignSuccess.tryEmit(Unit)
            }
            result.onFailure { e ->
                val apiEx = e as? ApiException
                val code = apiEx?.code
                val msg = apiEx?.message ?: e.localizedMessage ?: "위시 빈틈채우기에 실패했습니다."

                _errorCode.value = code
                _errorMessage.value = msg
                _errorState.value = code?.let { ApiException(it, msg) }

                _assignError.tryEmit(ApiException(code ?: "UNKNOWN", msg))
            }
        }
    }
}