package com.example.teumteum.utils

open class Event<out T>(private val content: T) {

    private var hasBeenHandled = false

    /**
     * 이벤트가 이미 처리되었는지 확인 후, 처리되지 않았다면 값을 반환
     */
    fun getContentIfNotHandled(): T? {
        return if (hasBeenHandled) {
            null
        } else {
            hasBeenHandled = true
            content
        }
    }

    /**
     * 이벤트의 원본 값을 반환 (처리 여부와 관계 없음)
     */
    fun peekContent(): T = content
}