package com.example.teumteum.data.remote.agreement.dto

import com.google.gson.annotations.SerializedName

data class AgreementRequest(
    @SerializedName("tosConsent") val tosConsent: Boolean,
    @SerializedName("privacyConsent") val privacyConsent: Boolean,
    @SerializedName("thirdPartyConsent") val thirdPartyConsent: Boolean,
    @SerializedName("marketingConsent") val marketingConsent: Boolean
)
