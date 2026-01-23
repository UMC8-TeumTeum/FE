package com.umc.teumteum.utils

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

enum class NextStep { AGREEMENT, ONBOARDING, MAIN }

@Singleton
class FlowPrefs @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs = context.getSharedPreferences("flow", Context.MODE_PRIVATE)

    fun getLastStep(): NextStep? =
        prefs.getString("last_step", null)?.let { runCatching { NextStep.valueOf(it) }.getOrNull() }

    fun setLastStep(step: NextStep) {
        prefs.edit().putString("last_step", step.name).apply()
    }

    fun clear() {
        prefs.edit().clear().apply()
    }
}
