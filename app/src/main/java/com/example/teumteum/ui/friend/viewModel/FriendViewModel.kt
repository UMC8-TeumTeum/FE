package com.example.teumteum.ui.friend.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.teumteum.data.AppUserManager
import com.example.teumteum.data.remote.friend.model.*
import com.example.teumteum.data.remote.friend.repository.FriendRepository
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.text.Collator
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class FriendViewModel @Inject constructor(
    private val repository: FriendRepository
) : ViewModel() {

    // 공통 메시지
    private val _successMessage = MutableLiveData<String>()
    val successMessage: LiveData<String> get() = _successMessage

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    //    상단 프로필의 star_btn 과 리스트 아이템의 starIv 가 함께 관찰하는 공통 상태
    private val _favoriteMap = MutableLiveData<Map<Int, Boolean>>(emptyMap())
    val favoriteMap: LiveData<Map<Int, Boolean>> get() = _favoriteMap

    // 1. 사용자 검색
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

    // 2. 틈 요청 조회(받은 목록)
    private val _receivedTeums = MutableLiveData<List<TeumReceivedItem>>()
    val receivedTeums: LiveData<List<TeumReceivedItem>> get() = _receivedTeums

    fun getTeumRequests() {
        viewModelScope.launch {
            repository.getReceivedTeums()
                .onSuccess { result ->
                    _receivedTeums.value = result
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

    // 3. 친구 프로필
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

    // 4. 틈 요청 보내기
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

    // 6. 약속된 틈 날짜 리스트 (달력 점)
    private val _scheduledDotDates = MutableLiveData<List<LocalDate>>()
    val scheduledDotDates: LiveData<List<LocalDate>> get() = _scheduledDotDates

    fun fetchScheduledTeumDates(month: String) {
        viewModelScope.launch {
            repository.getScheduledTeumCalendar(month)
                .onSuccess { resultList ->
                    _scheduledDotDates.value = resultList.mapNotNull {
                        runCatching { LocalDate.parse(it) }.getOrNull()
                    }
                    Log.d("CALENDAR_INFO", "약속된 틈 달력 정보 조회 성공")
                }
                .onFailure { e ->
                    Log.e("CALENDAR_INFO", "약속된 틈 달력 조회 실패: ${e.message}")
                }
        }
    }

    // 7. 특정 날짜의 약속된 틈 리스트
    private val _scheduledTeumList = MutableLiveData<List<TeumScheduledResult>>()
    val scheduledTeumList: LiveData<List<TeumScheduledResult>> get() = _scheduledTeumList

    fun fetchScheduledTeumList(date: String) {
        viewModelScope.launch {
            repository.getScheduledTeums(date)
                .onSuccess { result ->
                    _scheduledTeumList.value = result
                    _successMessage.value = "$date 약속된 틈 조회 성공 (총 ${result.size}개)"
                    Log.d("CALENDAR_SCHEDULED", "약속된 틈 $date 조회 결과: $result")
                }
                .onFailure { e ->
                    val msg = when {
                        e.message?.contains("TEUM4042") == true -> "약속된 틈이 존재하지 않습니다."
                        else -> "약속된 틈 조회 실패 (${e.message})"
                    }
                    _scheduledTeumList.value = emptyList()
                    _errorMessage.value = msg
                    Log.e("CALENDAR_SCHEDULED", "$date 조회 실패: ${e.message}", e)
                }
        }
    }

    // 8. 약속된 틈 상세 / 과거 여부
    private val _teumScheduleDetail = MutableLiveData<TeumScheduleDetailResult?>()
    val teumScheduleDetail: LiveData<TeumScheduleDetailResult?> get() = _teumScheduleDetail

    private val _isPastSchedule = MutableLiveData<Boolean?>()
    val isPastSchedule: LiveData<Boolean?> get() = _isPastSchedule

    fun fetchTeumScheduleDetail(teumId: Int) {
        viewModelScope.launch {
            repository.getTeumScheduleDetail(teumId)
                .onSuccess { result ->
                    _teumScheduleDetail.value = result
                    _successMessage.value = "약속된 틈 상세 정보가 조회되었습니다."

                    val now = LocalDateTime.now()
                    val dateTimeStr = "${result.date}T${result.startTime}" // yyyy-MM-dd'T'HH:mm
                    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")
                    val scheduleDateTime = runCatching {
                        LocalDateTime.parse(dateTimeStr, formatter)
                    }.getOrNull()

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

    // 9. 약속된 틈 취소
    fun cancelTeumSchedule(teumId: Int) {
        viewModelScope.launch {
            repository.cancelTeumSchedule(teumId)
                .onSuccess { result ->
                    _successMessage.value = "약속된 틈이 성공적으로 취소되었습니다."
                    Log.d("SCHEDULED_CANCEL", "취소된 유저 ID: ${result.cancelledUserIds}")
                }
                .onFailure { e ->
                    val msg = when {
                        e.message?.contains("TEUM4030") == true -> "정보에 대한 권한이 없습니다."
                        e.message?.contains("TEUM4042") == true -> "약속된 틈이 존재하지 않습니다."
                        else -> "약속된 틈 취소 실패 (${e.message})"
                    }
                    _errorMessage.value = msg
                    Log.e("SCHEDULED_CANCEL", "취소 실패: ${e.message}", e)
                }
        }
    }

    // 10. 특정 유저 팔로우
    private val _followMessage = MutableLiveData<String>()
    val followMessage: LiveData<String> get() = _followMessage

    fun followUser(userId: Int) {
        viewModelScope.launch {
            repository.followUser(userId)
                .onSuccess { response ->
                    Log.d("FOLLOW_FRAGMENT", "${response.code} | ${response.message}")
                    if (response.isSuccess) {
                        _followMessage.value = response.message
                    } else {
                        val msg = when (response.code) {
                            "FRIEND4002" -> "자기 자신은 팔로우할 수 없습니다."
                            "FRIEND4040" -> "존재하지 않는 유저입니다."
                            "FRIEND4001" -> "이미 팔로우한 유저입니다."
                            else -> "팔로우 실패: ${response.message}"
                        }
                        Log.e("FOLLOW_FRAGMENT", msg)
                        _followMessage.value = msg
                    }
                }
                .onFailure { e ->
                    Log.e("FOLLOW_FRAGMENT", "팔로우 요청 실패: ${e.message}")
                    _followMessage.value = "팔로우 요청 실패 (${e.message})"
                }
        }
    }

    // 11. 팔로잉 목록 조회(즐겨찾기 우선 + 닉네임 가나다 정렬)
    private val _followingUsers = MutableLiveData<List<FollowingResult>>()
    val followingUsers: LiveData<List<FollowingResult>> get() = _followingUsers

    fun getFollowingUsers(page: Int = 1, size: Int = 10) {
        viewModelScope.launch {
            repository.getFollowings(page, size)
                .onSuccess { list ->
                    val collator = Collator.getInstance(Locale.KOREAN).apply {
                        strength = Collator.PRIMARY
                    }
                    val sorted = list.sortedWith { a, b ->
                        // 즐겨찾기 true 먼저
                        if (a.isFavorite != b.isFavorite) return@sortedWith if (a.isFavorite) -1 else 1
                        // 닉네임 가나다순
                        collator.compare(a.nickname, b.nickname)
                    }
                    _followingUsers.value = sorted
                    Log.d("FOLLOWING_LIST", "FRIEND2002 친구 목록 조회 성공")
                }
                .onFailure { e ->
                    _errorMessage.value = "팔로잉 목록 조회 실패 (${e.message})"
                    Log.e("FOLLOWING_LIST", "팔로잉 목록 조회 실패: ${e.message}")
                }
        }
    }

    // 12. 특정 유저 언팔로우
    private val _unfollowMessage = MutableLiveData<String>()
    val unfollowMessage: LiveData<String> get() = _unfollowMessage

    fun unfollowUser(userId: Int) {
        viewModelScope.launch {
            repository.unfollowUser(userId)
                .onSuccess { response ->
                    if (response.isSuccess && response.code == "FRIEND2001") {
                        _unfollowMessage.value = response.message
                        Log.d("UNFOLLOW_FRAGMENT", "언팔로우가 성공적으로 완료되었습니다.")
                    } else {
                        val msg = when (response.code) {
                            "FRIEND4002" -> "자기 자신에 대한 요청은 처리할 수 없습니다."
                            "FRIEND4040" -> "존재하지 않는 유저입니다."
                            "FRIEND4005" -> "팔로우하지 않은 유저입니다."
                            else -> "언팔로우 실패: ${response.message}"
                        }
                        _unfollowMessage.value = msg
                        Log.e("UNFOLLOW_FRAGMENT", msg)
                    }
                }
                .onFailure { e ->
                    Log.e("UNFOLLOW_FRAGMENT", "언팔로우 요청 실패: ${e.message}", e)
                    _unfollowMessage.value = "언팔로우 요청 실패 (${e.message})"
                }
        }
    }

    // 13. 특정 유저 즐겨찾기 설정/해제
    private val _favoriteMessage = MutableLiveData<String>()
    val favoriteMessage: LiveData<String> get() = _favoriteMessage

    fun toggleFavorite(userId: Int) {
        val before = _favoriteMap.value?.get(userId) ?: false
        val after = !before

        // UI 즉시 반영
        _favoriteMap.value = _favoriteMap.value.orEmpty().toMutableMap().apply {
            put(userId, after)
        }
        _followingUsers.value = _followingUsers.value?.map { item ->
            if (item.userId == userId) item.copy(isFavorite = after) else item
        }

        viewModelScope.launch {
            repository.setFavorite(userId, after)
                .onSuccess { resp ->
                    if (resp.userId == userId && resp.isFavorite == after) {
                        _favoriteMessage.value =
                            if (after) "즐겨찾기에 추가했습니다." else "즐겨찾기를 해제했습니다."
                        Log.d("FAVORITE_FRAGMENT", _favoriteMessage.value ?: "")
                    } else {
                        // 서버 응답이 기대와 다르면 롤백
                        _favoriteMap.value = _favoriteMap.value.orEmpty().toMutableMap().apply {
                            put(userId, before)
                        }
                        _followingUsers.value = _followingUsers.value?.map { item ->
                            if (item.userId == userId) item.copy(isFavorite = before) else item
                        }
                        _favoriteMessage.value = "즐겨찾기 변경 실패(비정상 응답)"
                        Log.e("FAVORITE_FRAGMENT", _favoriteMessage.value ?: "")
                    }
                }
                .onFailure { e ->
                    // 실패 시 롤백
                    _favoriteMap.value = _favoriteMap.value.orEmpty().toMutableMap().apply {
                        put(userId, before)
                    }
                    _followingUsers.value = _followingUsers.value?.map { item ->
                        if (item.userId == userId) item.copy(isFavorite = before) else item
                    }
                    _favoriteMessage.value = "즐겨찾기 변경 실패 (${e.message})"
                    Log.e("FAVORITE_FRAGMENT", _favoriteMessage.value ?: "", e)
                }
        }
    }


}
