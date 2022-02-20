package com.longing.linphonecall

import android.content.Context
import org.linphone.core.LinphoneCoreFactory
import org.linphone.core.tutorials.TutorialHelloWorld

class LinphoneCore(private val context: Context) {
    fun getVersionCode(): String {
        val core = LinphoneCoreFactory.instance()
            .createLinphoneCore(TutorialHelloWorld(), context)
        return core.version
    }
}