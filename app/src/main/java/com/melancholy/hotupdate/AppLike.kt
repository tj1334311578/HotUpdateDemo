package com.melancholy.hotupdate

import android.app.Application
import android.content.Context
import android.content.Intent
import com.melancholy.hot.update.tinker.TinkerManager
import com.tencent.tinker.anno.DefaultLifeCycle
import com.tencent.tinker.entry.ApplicationLike
import com.tencent.tinker.loader.shareutil.ShareConstants


/**
 * @param application: 生成的application全类名
 * @param flags 表示Tinker支持的类型 dex only、library only or all suuport，default: TINKER_ENABLE_ALL
 * @param loaderClass Tinker的加载器，使用默认即可
 * @param loadVerifyFlag 加载dex或者lib是否验证md5，默认为false
 * @param useDelegateLastClassLoader
 */
@DefaultLifeCycle(application = "com.melancholy.hotupdate.App",
    flags = ShareConstants.TINKER_ENABLE_ALL, loadVerifyFlag = false)
class AppLike(application: Application, tinkerFlags: Int, tinkerLoadVerifyFlag: Boolean,
              applicationStartElapsedTime: Long, applicationStartMillisTime: Long,
              tinkerResultIntent: Intent?): ApplicationLike(application, tinkerFlags,
    tinkerLoadVerifyFlag, applicationStartElapsedTime, applicationStartMillisTime,
    tinkerResultIntent) {

    override fun onBaseContextAttached(base: Context?) {
        super.onBaseContextAttached(base)

        TinkerManager.init(this)
    }

}