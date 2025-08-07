package com.example.teumteum.ui.myhome.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.teumteum.data.remote.mypage.repository.MyPageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyHomeViewModel @Inject constructor(
    private val repository: MyPageRepository
) : ViewModel() {

    private val _nickname = MutableLiveData<String?>()
    val nickname: LiveData<String?> = _nickname

    private val _profileImageUrl = MutableLiveData<String?>()
    val profileImageUrl: LiveData<String?> = _profileImageUrl

    private val _field = MutableLiveData<String?>()
    val field: LiveData<String?> = _field

    //내 정보가 이미 조회되었는지 확인
    var isLoaded = false
        private set

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    /** 내 정보 조회 */
    fun getMyInfo() {
        viewModelScope.launch {
            repository.getMyInfo()
                .onSuccess { result ->
                    _nickname.value = result.nickname
                    _field.value = result.field
                    _profileImageUrl.value = result.profileImageUrl
                    isLoaded = true
                }
                .onFailure {
                    _error.value = "내 정보 조회 실패: ${it.message}"
                    Log.d("MyInfo", _error.value.toString() )
                }
        }
    }
}