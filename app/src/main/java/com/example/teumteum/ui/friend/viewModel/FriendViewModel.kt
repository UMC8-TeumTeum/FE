package com.example.teumteum.ui.friend.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.teumteum.data.AppUserManager
import com.example.teumteum.data.remote.friend.model.*
import com.example.teumteum.data.remote.friend.repository.FriendRepository
import com.example.teumteum.ui.friend.data.TimeCardItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class FriendViewModel @Inject constructor(
    private val repository: FriendRepository
) : ViewModel() {

    //  공통 메시지
    private val _successMessage = MutableLiveData<String>()
    val successMessage: LiveData<String> get() = _successMessage

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    //  1. 사용자 검색
    private val _searchResults = MutableLiveData<List<FriendSearchResult>>()
    val searchResults: LiveData<List<FriendSearchResult>> get() = _searchResults

    private val _recentKeywords = MutableLiveData<List<String>>(emptyList())
    val recentKeywords: LiveData<List<String>> get() = _recentKeywords

    fun searchUser(keyword: String) {
        viewModelScope.launch {
            repository.searchUser(keyword)
                .onSuccess { result ->
                    val myId = AppUserManager.userId
                    val filtered = result.filter { it.userId != myId }

                    when {
                        result.any { it.userId == myId } && filtered.isEmpty() -> {
                            _errorMessage.value = "존재하지 않는 사용자입니다."
                            _searchResults.value = emptyList()
                        }
                        filtered.isEmpty() -> {
                            _errorMessage.value = "존재하지 않는 사용자입니다."
                            _searchResults.value = emptyList()
                        }
                        else -> {
                            _searchResults.value = filtered
                            _successMessage.value = "사용자 조회 성공"
                            Log.d("VIEWMODEL", "성공 메시지 emit됨")
                        }
                    }
                }
                .onFailure { e ->
                    _searchResults.value = emptyList()
                    _errorMessage.value = "사용자 검색 실패 (${e.message})"
                }
        }
    }


    fun addRecentKeyword(keyword: String) {
        val updated = _recentKeywords.value.orEmpty() + keyword
        _recentKeywords.value = updated
    }

    fun removeLastKeyword() {
        val current = _recentKeywords.value.orEmpty()
        _recentKeywords.value = current.dropLast(1)
    }

    //  2. 틈 요청 조회
    private val _receivedTeums = MutableLiveData<List<TeumReceivedItem>>()
    val receivedTeums: LiveData<List<TeumReceivedItem>> get() = _receivedTeums

    fun getTeumRequests() {
        viewModelScope.launch {
            repository.getReceivedTeums()
                .onSuccess { result ->
                    _receivedTeums.value = result
                    _successMessage.value = "틈 요청 조회 성공 (총 ${result.size}개)"
                }
                .onFailure { e ->
                    val msg = when {
                        e.message?.contains("TEUM4030") == true -> "요청 또는 응답에 대한 권한이 없습니다."
                        else -> "틈 요청 조회 실패 (${e.message})"
                    }
                    _errorMessage.value = msg
                }
        }
    }

    //  3. 친구 프로필
    private val _friendProfile = MutableLiveData<FriendProfileResult>()
    val friendProfile: LiveData<FriendProfileResult> get() = _friendProfile

    fun getFriendProfile(userId: Int) {
        viewModelScope.launch {
            repository.getFriendProfile(userId)
                .onSuccess { result ->
                    _friendProfile.value = result
                    _successMessage.value = "친구 프로필 조회 성공"
                }
                .onFailure { e ->
                    val msg = when {
                        e.message?.contains("FRIEND4002") == true -> "자기 자신의 프로필은 조회할 수 없습니다."
                        e.message?.contains("FRIEND4040") == true -> "존재하지 않는 유저입니다."
                        else -> "친구 프로필 조회 실패 (${e.message})"
                    }
                    _errorMessage.value = msg
                }
        }
    }

    //  4. 틈 요청 보내기
    fun sendTeumRequest(request: TeumRequest) {
        viewModelScope.launch {
            repository.sendTeumRequest(request)
                .onSuccess { teumId ->
                    _successMessage.value = "틈 요청이 성공적으로 생성되었습니다. (id: $teumId)"
                }
                .onFailure { e ->
                    val msg = when {
                        e.message?.contains("TEUM4030") == true -> "요청 또는 응답에 대한 권한이 없습니다."
                        e.message?.contains("COMMON400") == true -> "잘못된 요청입니다."
                        e.message?.contains("TEUM4091") == true -> "자기 자신에게 틈 요청을 보낼 수 없습니다."
                        else -> "틈 요청 실패 (${e.message})"
                    }
                    _errorMessage.value = msg
                }
        }
    }

    // 5. 틈 응답 상태 변경
    fun respondToTeum(responseId: Int, status: String) {
        viewModelScope.launch {
            repository.respondToTeum(responseId, status)
                .onSuccess { result: TeumStatusResult ->
                    _successMessage.value = when (result.status) {
                        "ACCEPTED" -> "틈 요청을 수락했어요!"
                        "REJECTED" -> "틈 요청을 거절했어요."
                        else -> "응답 상태가 처리되었습니다."
                    }
                }
                .onFailure { e ->
                    val msg = when {
                        e.message?.contains("TEUM4002") == true -> "이미 마감된 요청입니다."
                        e.message?.contains("TEUM4030") == true -> "요청 또는 응답에 대한 권한이 없습니다."
                        e.message?.contains("TEUM4041") == true -> "존재하지 않는 틈 응답입니다."
                        e.message?.contains("TEUM4006") == true -> "응답 status 값은 accepted 또는 rejected 이어야 합니다."
                        else -> "틈 응답 실패 (${e.message})"
                    }
                    _errorMessage.value = msg
                }
        }
    }

    // class FriendViewModel 내부

    // 6. 약속된 틈 날짜 리스트 (달력 회색 점용)
    private val _scheduledDotDates = MutableLiveData<List<LocalDate>>()
    val scheduledDotDates: LiveData<List<LocalDate>> get() = _scheduledDotDates

    fun fetchScheduledTeumDates(month: String) {
        viewModelScope.launch {
            repository.getScheduledTeumCalendar(month)
                .onSuccess { resultList ->
                    _scheduledDotDates.value = resultList.mapNotNull {
                        try {
                            LocalDate.parse(it)
                        } catch (e: Exception) {
                            null
                        }
                    }
                    Log.d("CALENDER_INFO", "약속된 틈 달력 정보가 조회되었습니다.")
                }
                .onFailure { e ->
                    Log.e("CALENDER_INFO", "약속된 틈 조회 실패: ${e.message}")
                }
        }
    }

    // 7. 특정 날짜의 약속된 틈 리스트 조회
    private val _scheduledTeumList = MutableLiveData<List<TeumScheduledResult>>()
    val scheduledTeumList: LiveData<List<TeumScheduledResult>> get() = _scheduledTeumList

    fun fetchScheduledTeumList(date: String) {
        viewModelScope.launch {
            repository.getScheduledTeums(date)
                .onSuccess { result ->
                    _scheduledTeumList.value = result
                    _successMessage.value = "${date} 약속된 틈 조회 성공 (총 ${result.size}개)"
                    Log.d("CALENDER_SCHEDULED", "약속된 틈 ${date} 조회 결과: $result")

                }
                .onFailure { e ->
                    val msg = when {
                        e.message?.contains("TEUM4042") == true -> "약속된 틈이 존재하지 않습니다."
//                        e.message?.contains("TEUM4030") == true -> "해당 일정을 조회할 권한이 없습니다."
                        else -> "약속된 틈 조회 실패 (${e.message})"

                    }
                    _scheduledTeumList.value = emptyList() // 조회 실패 시 비워줌
                    _errorMessage.value = msg
                    Log.e("CALENDER_SCHEDULED", " ${date} 조회 실패: ${e.message}", e)
                }
        }
    }

    // 8. 특정 scheduleId의 약속된 틈 상세 정보
    private val _teumScheduleDetail = MutableLiveData<TeumScheduleDetailResult?>()
    val teumScheduleDetail: LiveData<TeumScheduleDetailResult?> get() = _teumScheduleDetail

    // 과거 여부 판단 (버튼 노출 판단용)
    private val _isPastSchedule = MutableLiveData<Boolean?>()
    val isPastSchedule: LiveData<Boolean?> get() = _isPastSchedule

    fun fetchTeumScheduleDetail(teumId: Int) {
        viewModelScope.launch {
            repository.getTeumScheduleDetail(teumId)
                .onSuccess { result ->

                    _teumScheduleDetail.value = result
                    _successMessage.value = "약속된 틈 상세 정보가 조회되었습니다."

                    // 현재 시간과 비교하여 과거 여부 판단
                    val now = java.time.LocalDateTime.now()
                    val dateTimeStr = "${result.date}T${result.startTime}"
                    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")
                    val scheduleDateTime = try {
                        java.time.LocalDateTime.parse(dateTimeStr, formatter)
                    } catch (e: Exception) {
                        null
                    }

                    _isPastSchedule.value = scheduleDateTime?.isBefore(now) == true

                }
                .onFailure { e ->
                    val msg = when {
                        e.message?.contains("TEUM4042") == true -> "약속된 틈이 존재하지 않습니다."
                        e.message?.contains("TEUM4030") == true -> "권한이 없습니다."
                        else -> "약속된 틈 상세 조회 실패 (${e.message})"
                    }
                    _errorMessage.value = msg
                    _teumScheduleDetail.value = null
                    _isPastSchedule.value = null
                    Log.e("TEUM_DETAIL", "상세 조회 실패: ${e.message}")
                }
        }
    }

    // 9. 약속된 틈 취소하기
    fun cancelTeumSchedule(teumId: Int) {
        viewModelScope.launch {
            repository.cancelTeumSchedule(teumId)
                .onSuccess { result ->
                    _successMessage.value = "약속된 틈이 성공적으로 취소되었습니다."
                    Log.d("SCHEDULED_CANCEL", " 취소된 유저 ID: ${result.cancelledUserIds}")
                }
                .onFailure { e ->
                    val msg = when {
                        e.message?.contains("TEUM4030") == true -> "정보에 대한 권한이 없습니다."
                        e.message?.contains("TEUM4042") == true -> "약속된 틈이 존재하지 않습니다."
                        else -> "약속된 틈 취소 실패 (${e.message})"
                    }
                    _errorMessage.value = msg
                    Log.e("SCHEDULED_CANCEL", " 취소 실패: ${e.message}", e)
                }
        }
    }

    fun readTeumRequest(responseId: Int) {
        viewModelScope.launch {
            repository.readTeumRequest(responseId)
                .onSuccess { result ->
                    _receivedTeums.value = _receivedTeums.value?.map { item ->
                        if (item.responseId == responseId) {
                            item.copy(read = true)
                        } else {
                            item
                        }
                    }
                    _successMessage.value = "틈 요청 읽음 처리 성공"
                }
                .onFailure { e ->
                    _errorMessage.value = "틈 요청 읽음 처리 실패 (${e.message})"
                    Log.d("ReadTeumRequest", _errorMessage.value.toString())
                }
        }
    }

    // 틈 요청자와 함께 가능한 빈틈(시간) 리스트
    private val _possibleTimeList = MutableLiveData<List<TimeCardItem?>>()
    val possibleTimeList: LiveData<List<TimeCardItem?>> get() = _possibleTimeList

    fun getPossibleTimeWithFriend(request: PossibleTimeRequest) {
        viewModelScope.launch {
            repository.getPossibleTime(request)
                .onSuccess { result ->

                    val mappedList = result.availableTime.map { available ->
                        TimeCardItem(
                            startTime = available.startTime,
                            endTime = available.endTime
                        )
                    }

                    _possibleTimeList.value = mappedList
                    _successMessage.value = "가능한 시간 조회 성공 (${mappedList.size}개)"
                    Log.d("POSSIBLE_TIME", "조회 결과: $mappedList")
                }
                .onFailure { e ->
                    _possibleTimeList.value = emptyList()
                    _errorMessage.value = "가능한 시간 조회 실패 (${e.message})"
                    Log.e("POSSIBLE_TIME", "조회 실패: ${e.message}", e)
                }
        }
    }

    fun resendTeumRequest(
        requestId: Int,
        body: ResendTeumRequest,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val result = repository.resendTeumRequest(requestId, body)

                result.onSuccess {
                    onSuccess()
                }.onFailure { e ->
                    onError(e.message ?: "재요청 실패")
                }

            } catch (e: Exception) {
                onError(e.message ?: "재요청 실패")
            }
        }
    }

}
