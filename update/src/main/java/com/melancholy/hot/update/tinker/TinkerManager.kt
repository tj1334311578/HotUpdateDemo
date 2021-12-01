package com.melancholy.hot.update.tinker

import android.annotation.SuppressLint
import android.app.Application
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.melancholy.hot.update.BuildConfig
import com.melancholy.hot.update.tinker.service.TinkerResultService
import com.tencent.tinker.entry.ApplicationLike
import com.tencent.tinker.lib.library.TinkerLoadLibrary
import com.tencent.tinker.lib.listener.DefaultPatchListener
import com.tencent.tinker.lib.listener.PatchListener
import com.tencent.tinker.lib.patch.AbstractPatch
import com.tencent.tinker.lib.patch.UpgradePatch
import com.tencent.tinker.lib.reporter.DefaultLoadReporter
import com.tencent.tinker.lib.reporter.DefaultPatchReporter
import com.tencent.tinker.lib.reporter.LoadReporter
import com.tencent.tinker.lib.reporter.PatchReporter
import com.tencent.tinker.lib.tinker.Tinker
import com.tencent.tinker.lib.tinker.TinkerInstaller
import com.tencent.tinker.lib.tinker.TinkerLoadResult
import com.tencent.tinker.loader.shareutil.ShareConstants

object TinkerManager {
    @SuppressLint("StaticFieldLeak")
    @Volatile
    @JvmStatic
    private var sTinker: Tinker? = null

    @Volatile
    @JvmStatic
    private var sInit: Boolean = false
    @Volatile
    @JvmStatic
    private lateinit var sApplication: Application
    @Volatile
    @JvmStatic
    private var sTinkerListener: TinkerListener? = null

    /**
     * 初始化
     */
    @JvmStatic
    @JvmOverloads
    fun init(applicationLike: ApplicationLike, loadReporter: LoadReporter? = null,
             patchReporter: PatchReporter? = null, patchListener: PatchListener? = null,
             patch: AbstractPatch? = null) {
        if(sTinker == null) {
            synchronized(TinkerManager::class.java) {
                if(sTinker == null) {
                    sApplication = applicationLike.application
                    sTinker = TinkerInstaller.install(applicationLike,
                        loadReporter ?: DefaultLoadReporter(sApplication),
                        patchReporter ?: DefaultPatchReporter (sApplication),
                        patchListener ?: DefaultPatchListener(sApplication),
                        TinkerResultService::class.java, patch ?: UpgradePatch())
                }
            }
        }
    }

    /**
     * 修复dex文件
     * @param path dex文件所在绝对路径
     */
    @JvmStatic
    @Synchronized
    fun onReceiveUpgradePatch(path: String) {
        if(sTinker != null) {
            TinkerInstaller.onReceiveUpgradePatch(sApplication, path)
        } else {
            throw RuntimeException("You must call the init method first")
        }
    }

    /**
     * 修复so
     * @param relativePath 库的路径
     * @param libName 库名称
     */
    @JvmStatic
    @Synchronized
    fun installNativeLibraryABI(relativePath: String, libName: String) {
        if(sTinker != null) {
            //3方式加载so
            //1.修改类加载器库路径
            //TinkerLoadLibrary.installNativeLibraryABI(context, "armeabi")
            //System.loadLibrary(libName)
            //2.对于lib/armeabi，只需使用TinkerInstaller.loadLibrary
            //TinkerLoadLibrary.loadArmLibrary(context.applicationContext, libName)
            //3.直接加载修补程序库
            TinkerLoadLibrary.loadLibraryFromTinker(sApplication, relativePath, libName)

        } else {
            throw RuntimeException("You must call the init method first")
        }
    }

    /**
     * 清理修复包
     */
    @JvmStatic
    @Synchronized
    fun cleanPatch() {
        if(sTinker != null) {
            sTinker!!.cleanPatch()
        } else {
            throw RuntimeException("You must call the init method first")
        }
    }

    @JvmStatic
    @Synchronized
    fun setTinkerListener(listener: TinkerListener?) {
        sTinkerListener = listener
    }

    @JvmStatic
    @Synchronized
    internal fun getTinkerListener(): TinkerListener? = sTinkerListener

    @JvmStatic
    @Synchronized
    fun getTinkerId(): String {
        return if(sTinker != null) {
            val info: ApplicationInfo = sApplication.packageManager
                .getApplicationInfo(sApplication.packageName, PackageManager.GET_META_DATA)
            info.metaData.getString(ShareConstants.TINKER_ID, "")
        } else {
            throw RuntimeException("You must call the init method first")
        }
    }

    @JvmStatic
    @Synchronized
    fun getTinkerVersion(): Int {
        return if(sTinker != null) {
            val version: String? = sTinker!!.tinkerLoadResultIfPresent
                ?.getPackageConfigByName("TINKER_VERSION")
            if(version.isNullOrEmpty()) 0 else version.toInt()
        } else {
            throw RuntimeException("You must call the init method first")
        }
    }
}