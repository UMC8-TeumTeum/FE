package com.umc.teumteum.data.remote.friend.model

data class PagingResponse<T>(
    val content: List<T>,
    val hasNext: Boolean
)