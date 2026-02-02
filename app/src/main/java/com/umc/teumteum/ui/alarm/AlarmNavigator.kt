package com.umc.teumteum.ui.alarm

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.umc.teumteum.data.remote.alarm.dto.NotificationResponse
import com.umc.teumteum.data.remote.alarm.dto.enums.NotificationType
import com.umc.teumteum.ui.friend.FriendFragment
import com.umc.teumteum.ui.friend.FriendPromiseFragment
import com.umc.teumteum.ui.friend.FriendTeumRequestFragment

object AlarmNavigator {

    fun createFragmentFor(host: Fragment, n: NotificationResponse): Fragment {
        val fm = host.parentFragmentManager
        val factory = fm.fragmentFactory
        val cl = host.requireContext().classLoader

        val className = when (n.type) {
            NotificationType.FOLLOW -> FriendFragment::class.java.name
            NotificationType.TEUM_REQUEST -> FriendFragment::class.java.name
            NotificationType.TEUM_ACCEPTED -> FriendPromiseFragment::class.java.name
            NotificationType.TEUM_DECLINED -> FriendTeumRequestFragment::class.java.name
            NotificationType.TEUM_REQUEST_REREQUEST -> FriendFragment::class.java.name
            NotificationType.TEUM_CANCELED -> FriendTeumRequestFragment::class.java.name
        }

        return factory.instantiate(cl, className).apply {
            arguments = (arguments ?: Bundle())
        }
    }
}
