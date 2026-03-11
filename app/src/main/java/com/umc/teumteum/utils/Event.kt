package com.umc.teumteum.utils

open class Event<out T>(private val content: T) {

    private var hasBeenHandled = false

    // 이벤트가 이미 처리되었는지 확인 후, 처리되지 않았다면 값을 반환
    fun getContentIfNotHandled(): T? {
        return if (hasBeenHandled) {
            null
        } else {
            hasBeenHandled = true
            content
        }
    }
}