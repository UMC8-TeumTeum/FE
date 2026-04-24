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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@HiltViewModel
class ProfileModifyViewModel @Inject constructor(
    private val repository: MyPageRepository
) : ViewModel() {

    sealed class ImageEditState {
        data object Keep : ImageEditState()
        data object Default : ImageEditState()
        data class New(val uri: Uri) : ImageEditState()
    }

    private val okHttpClient = OkHttpClient()

    private var originNickname: String? = null
    private var originField: String? = null
    private var originProfileUrl: String? = null

    private val _tempNickname = MutableLiveData<String?>(null)
    val tempNickname: LiveData<String?> = _tempNickname

    private val _tempField = MutableLiveData<String?>(null)
    val tempField: LiveData<String?> = _tempField

    private val _imageEditState = MutableLiveData<ImageEditState>(ImageEditState.Keep)
    val imageEditState: LiveData<ImageEditState> = _imageEditState

    private val _timePublic = MutableLiveData(true)
    val timePublic: LiveData<Boolean> = _timePublic

    private val _saveSuccess = MutableLiveData(false)
    val saveSuccess: LiveData<Boolean> = _saveSuccess

    private val _saveError = MutableLiveData<String?>(null)
    val saveError: LiveData<String?> = _saveError

    fun setTimePublic(value: Boolean) {
        _timePublic.value = value
    }

    fun startEdit(nickname: String?, field: String?, profileUrl: String?) {
        if (originNickname != null || originField != null || originProfileUrl != null) return

        originNickname = nickname
        originField = field
        originProfileUrl = profileUrl

        _tempNickname.value = nickname
        _tempField.value = field
        _imageEditState.value = ImageEditState.Keep
    }

    fun setTempNickname(v: String?) {
        _tempNickname.value = v
    }

    fun setTempField(v: String?) {
        _tempField.value = v
    }

    fun setTempImage(uri: Uri) {
        _imageEditState.value = ImageEditState.New(uri)
    }

    fun setDefaultProfileImage() {
        _imageEditState.value = ImageEditState.Default
    }

    fun keepCurrentProfileImage() {
        _imageEditState.value = ImageEditState.Keep
    }

    fun cancelEdit() {
        _tempNickname.value = originNickname
        _tempField.value = originField
        _imageEditState.value = ImageEditState.Keep
        clearSession()
    }

    fun commit(context: Context) {
        viewModelScope.launch {
            try {
                val nickname = _tempNickname.value?.trim().orEmpty()
                val jobField = _tempField.value?.trim().orEmpty()
                val timePublic = _timePublic.value ?: true
                val imageState = _imageEditState.value ?: ImageEditState.Keep

                repository.updateProfile(
                    ProfileUpdateRequest(
                        nickname = nickname,
                        jobField = jobField,
                        timePublic = timePublic
                    )
                ).getOrThrow()

                when (imageState) {
                    is ImageEditState.Keep -> Unit

                    is ImageEditState.Default -> {
                        repository.deleteProfileImage().getOrThrow()
                    }

                    is ImageEditState.New -> {
                        uploadProfileImageToS3AndRegister(context, imageState.uri)
                    }
                }

                _saveSuccess.value = true
                clearSession()
            } catch (e: Exception) {
                _saveError.value = e.message ?: "프로필 수정 실패"
            }
        }
    }

    private suspend fun uploadProfileImageToS3AndRegister(context: Context, uri: Uri) {
        val contentType = context.contentResolver.getType(uri) ?: "image/jpeg"

        val presignedResponse = repository
            .requestPresignedUrl(PresignedRequest(contentType))
            .getOrThrow()

        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw IllegalStateException("이미지를 불러올 수 없습니다.")

        val bytes = withContext(Dispatchers.IO) {
            inputStream.use { it.readBytes() }
        }

        uploadToS3(
            presignedUrl = presignedResponse.presignedUrl,
            bytes = bytes,
            contentType = contentType
        )

        repository.postProfileImage(
            ProfileImageRequest(presignedResponse.fileName)
        ).getOrThrow()
    }

    private suspend fun uploadToS3(
        presignedUrl: String,
        bytes: ByteArray,
        contentType: String
    ) = suspendCancellableCoroutine<Unit> { cont ->
        val requestBody = bytes.toRequestBody(contentType.toMediaTypeOrNull())
        val request = Request.Builder()
            .url(presignedUrl)
            .put(requestBody)
            .build()

        val call = okHttpClient.newCall(request)

        cont.invokeOnCancellation {
            call.cancel()
        }

        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                if (cont.isActive) cont.resumeWithException(e)
            }

            override fun onResponse(call: Call, response: okhttp3.Response) {
                response.use {
                    if (!cont.isActive) return
                    if (response.isSuccessful) {
                        cont.resume(Unit)
                    } else {
                        cont.resumeWithException(
                            IOException("이미지 업로드 실패 (code: ${response.code})")
                        )
                    }
                }
            }
        })
    }

    fun consumeSaveSuccess() {
        _saveSuccess.value = false
    }

    fun clearError() {
        _saveError.value = null
    }

    private fun clearSession() {
        originNickname = null
        originField = null
        originProfileUrl = null
        _imageEditState.postValue(ImageEditState.Keep)
    }
}