package com.longing.linphonecall

import android.content.Context
import org.linphone.core.Factory
class LinphoneCore(private val context: Context) {
    fun getVersionCode(): String {
        val factory = Factory.instance()
        // Some configuration can be done before the Core is created, for example enable debug logs.
        factory.setDebugMode(true, "Hello Linphone")

        // Your Core can use up to 2 configuration files, but that isn't mandatory.
        // On Android the Core needs to have the application context to work.
        // If you don't, the following method call will crash.
        val core = factory.createCore(null, null, context)
        return core.version

    }
}