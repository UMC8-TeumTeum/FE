package com.umc.teumteum.ui.myhome.viewModel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.umc.teumteum.data.remote.mypage.model.ProfileUpdateRequest
import com.umc.teumteum.data.remote.mypage.repository.MyPageRepository
import com.umc.teumteum.data.remote.onboarding.model.PresignedRequest
import com.umc.teumteum.data.remote.onboarding.model.ProfileImageRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class ProfileModifyViewModel @Inject constructor(
    private val repository: MyPageRepository
) : ViewModel() {

    private val okHttpClient = OkHttpClient()

    private var originNickname: String? = null
    private var originField: String? = null
    private var originProfileUrl: String? = null

    private val _tempNickname = MutableLiveData<String?>(null)
    val tempNickname: LiveData<String?> = _tempNickname

    private val _tempField = MutableLiveData<String?>(null)
    val tempField: LiveData<String?> = _tempField

    private val _tempImageUri = MutableLiveData<Uri?>(null)
    val tempImageUri: LiveData<Uri?> = _tempImageUri

    private val _timePublic = MutableLiveData(true)
    val timePublic: LiveData<Boolean> = _timePublic

    fun setTimePublic(value: Boolean) {
        _timePublic.value = value
    }

    // 저장 결과
    private val _saveSuccess = MutableLiveData(false)
    val saveSuccess: LiveData<Boolean> = _saveSuccess

    private val _saveError = MutableLiveData<String?>(null)
    val saveError: LiveData<String?> = _saveError

    fun startEdit(nickname: String?, field: String?, profileUrl: String?) {
        if (originNickname != null || originField != null || originProfileUrl != null) return

        originNickname = nickname
        originField = field
        originProfileUrl = profileUrl

        _tempNickname.value = nickname
        _tempField.value = field
        _tempImageUri.value = null
    }

    fun setTempNickname(v: String?) { _tempNickname.value = v }
    fun setTempField(v: String?) { _tempField.value = v }

    fun setTempImage(uri: Uri) {
        _tempImageUri.value = uri
    }

    fun cancelEdit() {
        _tempNickname.value = originNickname
        _tempField.value = originField
        _tempImageUri.value = null
        clearSession()
    }

    // 사용자 프로필 수정 반영
    fun commit(context: Context) {
        viewModelScope.launch {
            try {
                //이미지 업로드
                val uri = _tempImageUri.value
                if (uri != null) {
                    uploadProfileImageToS3AndRegister(context, uri)
                }

                //사용자 정보 수정
                val nickname = _tempNickname.value?.trim().orEmpty()
                val jobField = _tempField.value?.trim().orEmpty()
                val timePublic = _timePublic.value ?: true

                repository.updateProfile(
                    ProfileUpdateRequest(
                        nickname = nickname,
                        jobField = jobField,
                        timePublic = timePublic
                    )
                ).getOrThrow()

                _saveSuccess.value = true
                clearSession()
            } catch (e: Exception) {
                _saveError.value = e.message ?: "프로필 수정 실패"
            }
        }
    }

    private fun uploadProfileImageToS3AndRegister(context: Context, uri: Uri) {
        val contentType = context.contentResolver.getType(uri) ?: "image/jpeg"

        viewModelScope.launch {
            repository.requestPresignedUrl(PresignedRequest(contentType))
                .onSuccess { presignedResponse ->
                    val fileName = presignedResponse.fileName
                    val presignedUrl = presignedResponse.presignedUrl

                    val inputStream = context.contentResolver.openInputStream(uri)
                    val bytes = inputStream?.readBytes() ?: run {
                        _saveError.postValue("이미지를 불러올 수 없습니다.")
                        return@onSuccess
                    }

                    val requestBody = bytes.toRequestBody(contentType.toMediaTypeOrNull())
                    val request = Request.Builder()
                        .url(presignedUrl)
                        .put(requestBody)
                        .build()

                    okHttpClient.newCall(request).enqueue(object : Callback {
                        override fun onFailure(call: okhttp3.Call, e: IOException) {
                            _saveError.postValue("이미지 업로드 실패: ${e.message}")
                        }

                        override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                            if (response.isSuccessful) {
                                postProfileImage(ProfileImageRequest(fileName))
                            } else {
                                _saveError.postValue("이미지 업로드 실패 (code: ${response.code})")
                            }
                        }
                    })
                }
                .onFailure { e ->
                    _saveError.postValue(e.message ?: "프리사인드 URL 발급 실패")
                }
        }
    }

    private fun postProfileImage(request: ProfileImageRequest) {
        viewModelScope.launch {
            repository.postProfileImage(request)
                .onSuccess {
                    _saveSuccess.postValue(true)
                    clearSession()
                }
                .onFailure { e ->
                    _saveError.postValue(e.message ?: "프로필 이미지 등록 실패")
                }
        }
    }

    fun consumeSaveSuccess() { _saveSuccess.value = false }
    fun clearError() { _saveError.value = null }

    private fun clearSession() {
        originNickname = null
        originField = null
        originProfileUrl = null
    }
}