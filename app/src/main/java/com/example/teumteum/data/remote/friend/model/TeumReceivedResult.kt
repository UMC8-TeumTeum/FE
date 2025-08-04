package com.example.teumteum.data.remote.friend.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

data class TeumReceivedResult(
    @SerializedName("totalElements") val totalElements: Long,
    @SerializedName("totalPages") val totalPages: Int,
    @SerializedName("size") val size: Int,
    @SerializedName("content") val content: List<TeumReceivedItem>,
    @SerializedName("number") val number: Int,
    @SerializedName("sort") val sort: SortInfo,
    @SerializedName("numberOfElements") val numberOfElements: Int,
    @SerializedName("pageable") val pageable: PageableInfo,
    @SerializedName("first") val first: Boolean,
    @SerializedName("last") val last: Boolean,
    @SerializedName("empty") val empty: Boolean
)

@Parcelize
data class TeumReceivedItem(
    @SerializedName("responseId") val responseId: Int,
    @SerializedName("requestId") val requestId: Int,
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String,
    @SerializedName("graphicId") val graphicId: Int,
    @SerializedName("senderUser") val senderUser: TeumSenderUser,
    @SerializedName("receiverCount") val receiverCount: Int,
    @SerializedName("date") val date: String,
    @SerializedName("timeSlot") val timeSlot: TeumTimeSlot,
    @SerializedName("read") val read: Boolean
) : Parcelable

@Parcelize
data class TeumSenderUser(
    @SerializedName("userId") val userId: Int,
    @SerializedName("nickname") val nickname: String,
    @SerializedName("profileImageUrl") val profileImageUrl: String
) : Parcelable

@Parcelize
data class TeumTimeSlot(
    @SerializedName("start") val start: String,
    @SerializedName("end") val end: String
) : Parcelable

@Parcelize
data class SortInfo(
    @SerializedName("empty") val empty: Boolean,
    @SerializedName("sorted") val sorted: Boolean,
    @SerializedName("unsorted") val unsorted: Boolean
) : Parcelable

@Parcelize
data class PageableInfo(
    @SerializedName("offset") val offset: Long,
    @SerializedName("sort") val sort: SortInfo,
    @SerializedName("paged") val paged: Boolean,
    @SerializedName("pageNumber") val pageNumber: Int,
    @SerializedName("pageSize") val pageSize: Int,
    @SerializedName("unpaged") val unpaged: Boolean
) : Parcelable

