package com.example.teumteum.utils

object ActivityRequestUtils {
    private val categoryMap = mapOf(
        "자기계발" to 1L,
        "운동" to 2L,
        "취미" to 3L,
        "일상" to 4L,
        "문화생활" to 5L,
        "휴식" to 6L
    )

    fun getEstimatedDurationType(tag: String): String {
        return when (tag) {
            "10m" -> "10m"
            "20m" -> "20m"
            "30m" -> "30m"
            "1h" -> "1h"
            else -> throw IllegalArgumentException("유효하지 않은 시간 태그: $tag")
        }
    }

    fun getCategoryIdIfExists(category: String?): Long? {
        return categoryMap[category]
    }

    fun getCustomCategoryIfOther(selectedCategory: String?, userInput: String?): String? {
        return if (getCategoryIdIfExists(selectedCategory) == null) {
            userInput?.takeIf { it.isNotBlank() }
        } else null
    }
}