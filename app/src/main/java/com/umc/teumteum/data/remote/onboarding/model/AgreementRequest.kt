package com.umc.teumteum.data.remote.onboarding.model

import com.google.gson.annotations.SerializedName

data class AgreementRequest(
    @SerializedName("tosConsent") val tosConsent: Boolean,
    @SerializedName("privacyConsent") val privacyConsent: Boolean,
    @SerializedName("thirdPartyConsent") val thirdPartyConsent: Boolean,
    @SerializedName("marketingConsent") val marketingConsent: Boolean
)