package com.makfc.xposed_all_apps_allow_audio_playback_capture

import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam

const val PRIVATE_FLAG_ALLOW_AUDIO_PLAYBACK_CAPTURE = 1 shl 27

fun log(text: String) {
    XposedBridge.log("${BuildConfig.APPLICATION_ID}: $text")
}

class Main : IXposedHookLoadPackage {
    override fun handleLoadPackage(lpparam: LoadPackageParam) {
        when (lpparam.packageName) {
            "android" -> {
                XposedBridge.hookAllMethods(
                    XposedHelpers.findClass(
                        "com.android.server.pm.PackageManagerService",
                        lpparam.classLoader
                    ), "getPackageInfo", object : XC_MethodHook() {
                        override fun afterHookedMethod(param: MethodHookParam) {
                            val packageInfo = param.result as PackageInfo?
                            if (packageInfo != null) {
                                val appInfo = packageInfo.applicationInfo
                                var privateFlags =
                                    XposedHelpers.getObjectField(appInfo, "privateFlags") as Int

                                if (privateFlags and PRIVATE_FLAG_ALLOW_AUDIO_PLAYBACK_CAPTURE == 0) {
                                    privateFlags =
                                        privateFlags or PRIVATE_FLAG_ALLOW_AUDIO_PLAYBACK_CAPTURE
                                }
                                XposedHelpers.setObjectField(appInfo, "privateFlags", privateFlags)
                                param.result = packageInfo
//                                isAllowCapture(appInfo)
                            }
                        }
                    })
            }
        }
    }

    fun isAllowCapture(info: ApplicationInfo): Boolean {
        try {
            val privateFlags = XposedHelpers.getObjectField(info, "privateFlags") as Int
            if (privateFlags and PRIVATE_FLAG_ALLOW_AUDIO_PLAYBACK_CAPTURE != 0) {
                return true
            }
        } catch (e: Exception) {
        }
        return false
    }
}