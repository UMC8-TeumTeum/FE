package com.example.teumteum.ui.friend.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.teumteum.data.AppUserManager
import com.example.teumteum.data.remote.friend.model.*
import com.example.teumteum.data.remote.friend.repository.FriendRepository

import com.example.teumteum.utils.Event
import com.google.gson.Gson
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay

import com.example.teumteum.ui.friend.data.TimeCardItem

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.text.Collator
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject
import kotlin.math.E

@HiltViewModel
class FriendViewModel @Inject constructor(
    private val repository: FriendRepository,
    private val myPageRepository: com.example.teumteum.data.remote.mypage.repository.MyPageRepository
) : ViewModel() {

    private var searchJob: Job? = null
    private var lastKeyword: String? = null

    // 공통 메시지
    private val _successMessage = MutableLiveData<Event<String>>()
    val successMessage: LiveData<Event<String>> get() = _successMessage

    private val _errorMessage = MutableLiveData<Event<String>>()
    val errorMessage: LiveData<Event<String>> get() = _errorMessage


    //    상단 프로필의 star_btn 과 리스트 아이템의 starIv 가 함께 관찰하는 공통 상태
    private val _favoriteMap = MutableLiveData<Map<Int, Boolean>>(emptyMap())
    val favoriteMap: LiveData<Map<Int, Boolean>> get() = _favoriteMap

    private val _myNickname = MutableLiveData<String>()
    val myNickname: LiveData<String> get() = _myNickname

    private val _myProfileUrl = MutableLiveData<String>()
    val myProfileUrl: LiveData<String> get() = _myProfileUrl

    // 선택된 친구 목록 저장용
    private val _selectedFriends =
        MutableLiveData<MutableList<FriendProfileResult>>(mutableListOf())
    val selectedFriends: LiveData<MutableList<FriendProfileResult>> get() = _selectedFriends

    // 선택된 친구 추가
    fun addSelectedFriend(friend: FriendProfileResult) {
        val currentList = _selectedFriends.value ?: mutableListOf()
        // 중복 방지
        if (currentList.none { it.userId == friend.userId }) {
            currentList.add(friend)
            _selectedFriends.value = currentList
        }
    }


    fun fetchMyInfo() {
        viewModelScope.launch {
            myPageRepository.getMyInfo()
                .onSuccess { info: com.example.teumteum.data.remote.mypage.model.MyInfoResponse ->
                    _myNickname.value = info.nickname
                    _myProfileUrl.value = info.profileImageUrl
                }
                .onFailure { e: Throwable ->
                    Log.e("MY_INFO", "내 정보 조회 실패: ${e.message}", e)
                }
        }
    }


    // 1. 사용자 검색
    private val _searchResults = MutableLiveData<List<FriendSearchResult>>()
    val searchResults: LiveData<List<FriendSearchResult>> get() = _searchResults

    private val _recentKeywords = MutableLiveData<List<String>>(emptyList())
    val recentKeywords: LiveData<List<String>> get() = _recentKeywords

    fun searchUser(keyword: String) {
        if (keyword == lastKeyword) return
        lastKeyword = keyword

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(250) // 디바운스
            repository.searchUser(keyword)
                .onSuccess { result ->
                    val myId = AppUserManager.userId
                    val filtered = result.filter { it.userId != myId }

                    if (filtered.isEmpty()) {
                        _searchResults.value = emptyList()
                        _errorMessage.value = Event("존재하지 않는 사용자입니다.")
                    } else {
                        _searchResults.value = filtered
//                        _successMessage.value = Event("사용자 조회 성공")  // 확인용 토스트
//                        Log.d("VIEWMODEL", "성공 메시지 emit됨") // 확인용 로그
                    }
                }
                .onFailure { e ->
                    _searchResults.value = emptyList()
                    _errorMessage.value = Event("사용자 검색 실패 (${e.message})")
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
                    _errorMessage.value = Event(msg)
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
                    _successMessage.value = Event("친구 프로필 조회 성공")
                }
                .onFailure { e ->
                    val msg = when {
                        e.message?.contains("FRIEND4002") == true -> "자기 자신의 프로필은 조회할 수 없습니다."
                        e.message?.contains("FRIEND4040") == true -> "존재하지 않는 유저입니다."
                        else -> "친구 프로필 조회 실패 (${e.message})"
                    }
                    _errorMessage.value = Event(msg)
                }
        }
    }

    // 4. 틈 요청 보내기
    fun sendTeumRequest(request: TeumRequest) {
        viewModelScope.launch {
            repository.sendTeumRequest(request)
                .onSuccess { teumId ->
                    _successMessage.value = Event("틈 요청이 성공적으로 생성되었습니다. (id: $teumId)")
                }
                .onFailure { e ->
                    val msg = when {
                        e.message?.contains("TEUM4030") == true -> "요청 또는 응답에 대한 권한이 없습니다."
                        e.message?.contains("COMMON400") == true -> "잘못된 요청입니다."
                        e.message?.contains("TEUM4091") == true -> "자기 자신에게 틈 요청을 보낼 수 없습니다."
                        else -> "틈 요청 실패 (${e.message})"
                    }
                    _errorMessage.value = Event(msg)
                }
        }
    }

    // 5. 틈 응답 상태 변경
    fun respondToTeum(responseId: Int, status: String) {
        viewModelScope.launch {
            repository.respondToTeum(responseId, status)
                .onSuccess { result: TeumStatusResult ->
                    _successMessage.value = when (result.status) {
                        "ACCEPTED" -> Event("틈 요청을 수락했어요!")
                        "REJECTED" -> Event("틈 요청을 거절했어요.")
                        else -> Event("응답 상태가 처리되었습니다.")
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
                    _errorMessage.value = Event(msg)
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
                    _successMessage.value = Event("$date 약속된 틈 조회 성공 (총 ${result.size}개)")
                    Log.d("CALENDAR_SCHEDULED", "약속된 틈 $date 조회 결과: $result")
                }
                .onFailure { e ->
                    val msg = when {
                        e.message?.contains("TEUM4042") == true -> "약속된 틈이 존재하지 않습니다."
                        else -> "약속된 틈 조회 실패 (${e.message})"
                    }
                    _scheduledTeumList.value = emptyList()
                    _errorMessage.value = Event(msg)
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
                    _successMessage.value = Event("약속된 틈 상세 정보가 조회되었습니다.")

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
                    _errorMessage.value = Event(msg)
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
                    _successMessage.value = Event("약속된 틈이 성공적으로 취소되었습니다.")
                    Log.d("SCHEDULED_CANCEL", "취소된 유저 ID: ${result.cancelledUserIds}")
                }
                .onFailure { e ->
                    val msg = when {
                        e.message?.contains("TEUM4030") == true -> "정보에 대한 권한이 없습니다."
                        e.message?.contains("TEUM4042") == true -> "약속된 틈이 존재하지 않습니다."
                        else -> "약속된 틈 취소 실패 (${e.message})"
                    }
                    _errorMessage.value = Event(msg)
                    Log.e("SCHEDULED_CANCEL", "취소 실패: ${e.message}", e)
                }
        }
    }

    // 10. 특정 유저 팔로우
    private val _followMessage = MutableLiveData<Event<String>>()
    val followMessage: LiveData<Event<String>> get() = _followMessage

    fun followUser(userId: Int) {
        viewModelScope.launch {
            repository.followUser(userId)
                .onSuccess { response ->
                    Log.d("FOLLOW_FRAGMENT", "${response.code} | ${response.message}")
                    val msg = if (response.isSuccess) {
                        response.message
                    } else {
                        when (response.code) {
                            "FRIEND4002" -> "자기 자신은 팔로우할 수 없습니다."
                            "FRIEND4040" -> "존재하지 않는 유저입니다."
                            "FRIEND4001" -> "이미 팔로우한 유저입니다."
                            else -> "팔로우 실패: ${response.message}"
                        }
                    }
                    _followMessage.value = Event(msg)
                }
                .onFailure { e ->
                    val msg = "팔로우 요청 실패 (${e.message})"
                    Log.e("FOLLOW_FRAGMENT", msg)
                    _followMessage.value = Event(msg)
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
                    // 기존 토글 상태 우선 반영
                    val favMap = _favoriteMap.value.orEmpty()
                    val merged = list.map { item ->
                        val fav = favMap[item.userId] ?: item.isFavorite
                        item.copy(isFavorite = fav)
                    }

                    val collator =
                        Collator.getInstance(Locale.KOREAN).apply { strength = Collator.PRIMARY }
                    val sorted = merged.sortedWith(Comparator { a, b ->
                        if (a.isFavorite != b.isFavorite) {
                            if (a.isFavorite) -1 else 1
                        } else {
                            collator.compare(a.nickname, b.nickname)
                        }
                    })

                    _followingUsers.value = sorted
                    Log.d("FOLLOWING_LIST", "FRIEND2002 친구 목록 조회 성공")
                }
                .onFailure { e ->
                    _errorMessage.value = Event("팔로잉 목록 조회 실패 (${e.message})")
                    Log.e("FOLLOWING_LIST", "팔로잉 목록 조회 실패: ${e.message}")
                }
        }
    }

    // 12. 특정 유저 언팔로우
    private val _unfollowMessage = MutableLiveData<Event<String>>()
    val unfollowMessage: LiveData<Event<String>> get() = _unfollowMessage

    fun unfollowUser(userId: Int) {
        viewModelScope.launch {
            repository.unfollowUser(userId)
                .onSuccess { response ->
                    if (response.isSuccess && response.code == "FRIEND2001") {
                        // 1) 언팔로우 메시지
                        _unfollowMessage.value = Event(response.message)

                        // 2) 팔로잉 목록에서 해당 유저 제거
                        _followingUsers.value = _followingUsers.value
                            ?.filter { it.userId != userId }

                        // 3) 즐겨찾기 상태도 해제(또는 제거)
                        _favoriteMap.value = _favoriteMap.value.orEmpty()
                            .toMutableMap().apply {
                                // put(userId, false) 로 해제하거나,
                                // remove(userId) 로 키 자체를 없애도 됨. 여기선 해제로 유지.
                                put(userId, false)
                            }

                        Log.d("UNFOLLOW_FRAGMENT", "언팔로우 성공")
                    } else {
                        val msg = when (response.code) {
                            "FRIEND4002" -> "자기 자신에 대한 요청은 처리할 수 없습니다."
                            "FRIEND4040" -> "존재하지 않는 유저입니다."
                            "FRIEND4005" -> "팔로우하지 않은 유저입니다."
                            else -> "언팔로우 실패: ${response.message}"
                        }
                        _unfollowMessage.value = Event(msg)
                        Log.e("UNFOLLOW_FRAGMENT", msg)
                    }
                }
                .onFailure { e ->
                    val msg = "언팔로우 요청 실패 (${e.message})"
                    Log.e("UNFOLLOW_FRAGMENT", msg, e)
                    _unfollowMessage.value = Event(msg)
                }
        }
    }

    // 13. 특정 유저 즐겨찾기 설정/해제
    private val _favoriteMessage = MutableLiveData<String>()
    val favoriteMessage: LiveData<String> get() = _favoriteMessage

    fun toggleFavorite(userId: Int) {
        val before = _favoriteMap.value?.get(userId) ?: false
        val after = !before

        // 즐겨찾기 맵 즉시 반영
        _favoriteMap.value = _favoriteMap.value.orEmpty().toMutableMap().apply {
            put(userId, after)
        }

        // 즐겨찾기 → 닉네임 가나다 정렬 (한글 Collator 사용)
        val collator = Collator.getInstance(Locale.KOREAN).apply { strength = Collator.PRIMARY }
        _followingUsers.value = _followingUsers.value
            ?.map { item -> if (item.userId == userId) item.copy(isFavorite = after) else item }
            ?.sortedWith(Comparator { a, b ->
                if (a.isFavorite != b.isFavorite) {
                    if (a.isFavorite) -1 else 1
                } else {
                    collator.compare(a.nickname, b.nickname)
                }
            })

        viewModelScope.launch {
            repository.setFavorite(userId, after)
                .onSuccess { resp ->
                    if (resp.userId == userId && resp.isFavorite == after) {
                        _favoriteMessage.value = if (after) {
                            Log.d("FAVORITE_FRAGMENT", "즐겨찾기 성공 → userId=$userId")
                            "즐겨찾기에 추가했습니다."
                        } else {
                            Log.d("FAVORITE_FRAGMENT", "즐겨찾기 해제 성공 → userId=$userId")
                            "즐겨찾기를 해제했습니다."
                        }
                    } else {
                        rollbackFavorite(userId, before)
                        _favoriteMessage.value = "즐겨찾기 변경 실패"
                        Log.e("FAVORITE_FRAGMENT", "비정상 응답 → userId=$userId")
                    }
                }
                .onFailure { e ->
                    rollbackFavorite(userId, before)
                    _favoriteMessage.value = "즐겨찾기 변경 실패 (${e.message})"
                    Log.e("FAVORITE_FRAGMENT", "서버 요청 실패 → userId=$userId", e)
                }
        }
    }

    private fun rollbackFavorite(userId: Int, before: Boolean) {
        _favoriteMap.value = _favoriteMap.value.orEmpty().toMutableMap().apply {
            put(userId, before)
        }

        val collator = Collator.getInstance(Locale.KOREAN).apply { strength = Collator.PRIMARY }
        _followingUsers.value = _followingUsers.value
            ?.map { item -> if (item.userId == userId) item.copy(isFavorite = before) else item }
            ?.sortedWith(Comparator { a, b ->
                if (a.isFavorite != b.isFavorite) {
                    if (a.isFavorite) -1 else 1
                } else {
                    collator.compare(a.nickname, b.nickname)
                }
            })
    }

    // 14. 팔로워 목록 조회
    private val _followerUsers = MutableLiveData<List<FollowerResult>>()
    val followerUsers: LiveData<List<FollowerResult>> get() = _followerUsers

    // 다음 페이지 여부
    private val _followersHasNext = MutableLiveData<Boolean>()
    val followersHasNext: LiveData<Boolean> get() = _followersHasNext

    /** 팔로워 목록 조회 (가나다 정렬 + 성공 로그/토스트) */
    fun getFollowerUsers(page: Int = 1, size: Int = 10) {
        viewModelScope.launch {
            // repository에 getFollowersPage(...) 추가해둔 버전 사용
            repository.getFollowersPage(page, size)
                .onSuccess { pageResult ->
                    Log.d("FOLLOWER_FRAGMENT", "친구 목록 조회에 성공하였습니다.")

                    // 가나다 정렬
                    val collator =
                        Collator.getInstance(Locale.KOREAN).apply { strength = Collator.PRIMARY }
                    val sorted = pageResult.content.sortedWith { a, b ->
                        collator.compare(a.nickname, b.nickname)
                    }

                    _followerUsers.value = sorted
                    _followersHasNext.value = pageResult.hasNext

//                    _successMessage.value = Event("친구 목록 조회에 성공하였습니다.")
                }
                .onFailure { e ->
                    val msg = "팔로워 목록 조회 실패 (${e.message ?: "알 수 없는 오류"})"
                    _errorMessage.value = Event(msg)
                    Log.e("FOLLOWER_FRAGMENT", msg, e)
                }
        }
    }

    // 틈 읽기
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
                    _successMessage.value = Event("틈 요청 읽음 처리 성공")
                }
                .onFailure { e ->
                    _errorMessage.value = Event("틈 요청 읽음 처리 실패 (${e.message})")
                    Log.d("ReadTeumRequest", _errorMessage.value.toString())
                }
        }
    }

    // 15. 맞팔로우 목록
    private val _mutualFriends = MutableLiveData<List<MutualFriendItem>>()
    val mutualFriends: LiveData<List<MutualFriendItem>> get() = _mutualFriends

    fun getMutualFriends(excludeUserId: Int? = null) {
        viewModelScope.launch {
            val myUserId = AppUserManager.userId

            // 자기 자신 제외 방지
            if (excludeUserId != null && excludeUserId == myUserId) {
                _errorMessage.value = Event("자기 자신은 제외할 수 없습니다.")
                return@launch
            }

            repository.getMutualFriends(
                if (excludeUserId == null || excludeUserId == -1) null else excludeUserId
            )
                .onSuccess { list ->
                    _mutualFriends.value = list
                    _successMessage.value = Event("친구 목록 조회에 성공하였습니다.")
                    Log.d("MUTUAL_FRAGMENT", "친구 목록 조회 성공")
                }
                .onFailure { e ->
                    val msg = when {
                        e.message?.contains("FRIEND4002") == true -> "자기 자신에 대한 요청은 처리할 수 없습니다."
                        e.message?.contains("FRIEND4040") == true -> "존재하지 않는 유저입니다."
                        else -> "맞팔로우 목록 조회 실패 (${e.message})"
                    }
                    _errorMessage.value = Event(msg)
                    Log.e("MUTUAL_FRAGMENT", msg, e)
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
                    _successMessage.value = Event("가능한 시간 조회 성공 (${mappedList.size}개)")
                    Log.d("POSSIBLE_TIME", "조회 결과: $mappedList")
                }
                .onFailure { e ->
                    _possibleTimeList.value = emptyList()
                    _errorMessage.value = Event("가능한 시간 조회 실패 (${e.message})")
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
            repository.resendTeumRequest(requestId, body)
                .onSuccess { onSuccess() }
                .onFailure { e -> onError(e.message ?: "재요청 실패") }
        }
    }
}
