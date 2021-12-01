/*
package com.melancholy.hotupdate.l

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Application
import android.content.*
import android.content.pm.PackageManager
import android.content.res.AssetManager
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.util.Log
import com.melancholy.hot.update.util.Constants
import com.melancholy.hot.update.util.Utils
import com.tencent.tinker.loader.TinkerLoader
import com.tencent.tinker.loader.app.TinkerApplication
import com.tencent.tinker.loader.shareutil.ShareConstants
import java.lang.ref.WeakReference
import java.lang.reflect.Constructor
import java.lang.reflect.Field
import java.lang.reflect.Method

*/
/**
 * Tinker真实Application
 * @param tinkerFlags 可以修复那些文件
 * @param delegateClassName 代理的ApplicationLike全类名
 * @param loaderClassName 加载的loader全类名
 * @param tinkerLoadVerityFlag
 * @param useDelegateLastClassLoader
 *//*

internal class TinkerApplication: TinkerApplication(ShareConstants.TINKER_ENABLE_ALL,
    TinkerApplicationLike::class.java.name, TinkerLoader::class.java.name, false,
    true) {
    //真实的application
    private var mRealApplication: Application? = null
    //真实的application的全类名
    private var mRawApplicationName: String? = null
    private var isReflectFailure = false

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        //反射创建真实的application
        try {
            val realApplicationName: String? = getRawApplicationName(base)
            if(realApplicationName == null) {
                Log.d(Constants.TAG, "can get real realApplication from manifest!")
            } else {
                val constructor: Constructor<*> = Class.forName(realApplicationName, false,
                    this.classLoader).getConstructor()
                mRealApplication = constructor.newInstance() as Application
            }
        } catch (e: Exception) {
            throw IllegalStateException(e)
        }

        //通过反射调用mRealApplication的attachBaseContext方法
        if (mRealApplication != null) {
            try {
                val attachBaseContext: Method = ContextWrapper::class.java
                    .getDeclaredMethod("attachBaseContext", Context::class.java)
                attachBaseContext.isAccessible = true
                attachBaseContext.invoke(mRealApplication, base)
            } catch (e: Exception) {
                throw IllegalStateException(e)
            }
        }
    }

    @SuppressLint("PrivateApi", "DiscouragedPrivateApi")
    override fun onCreate() {
        if (mRealApplication != null) {
            try {
                //1.反射获取ActivityThread类
                val activityThread: Class<*> = Class.forName("android.app.ActivityThread")
                val currentActivityThread: Any? = Utils.getActivityThread(this, activityThread)
                //2.将ActivityThread类中的mInitialApplication修改为mRealApplication
                val mInitialApplicationField: Field = activityThread
                    .getDeclaredField("mInitialApplication")
                mInitialApplicationField.isAccessible = true
                val oldApplication: Application = mInitialApplicationField.get(currentActivityThread)
                        as Application
                if (mRealApplication != null && oldApplication == this) {
                    mInitialApplicationField.set(currentActivityThread, mRealApplication)
                }

                //3.将ActivityThread类中的mAllApplications集合修改为mRealApplication
                if (this.mRealApplication != null) {
                    val mAllApplicationsField: Field = activityThread
                        .getDeclaredField("mAllApplications")
                    mAllApplicationsField.isAccessible = true
                    val mAllApplications: MutableList<Application> = mAllApplicationsField
                        .get(currentActivityThread) as MutableList<Application>
                    for(i: Int in mAllApplications.indices) {
                        if (mAllApplications[i] == this) {
                            mAllApplications[i] = mRealApplication!!
                        }
                    }
                }

                //4.需要修改ActivityThread类中的mPackages和mResourcePackages
                val var21 = try {
                    //获取LoadedApk类，如果失败了，就获取ActivityThread$PackageInfo
                    Class.forName("android.app.LoadedApk")
                } catch (e: ClassNotFoundException) {
                    Class.forName("android.app.ActivityThread\$PackageInfo")
                }

                val var22: Field = var21.getDeclaredField("mApplication")
                var22.isAccessible = true
                var var23: Field? = null
                try {
                    var23 = Application::class.java.getDeclaredField("mLoadedApk")
                } catch (e: NoSuchFieldException) {
                    e.printStackTrace()
                }
                val var9: Array<String> = arrayOf("mPackages", "mResourcePackages")
                for(i: Int in var9.indices) {
                    val var11: String = var9[i]
                    val var12: Field = activityThread.getDeclaredField(var11)
                    var12.isAccessible = true
                    val var13: Any = var12.get(currentActivityThread)!!
                    val var14: Iterator<*> = (var13 as Map<*, *>).entries.iterator()

                    while(var14.hasNext()) {
                        val var15: java.util.Map.Entry<*, *> = var14.next() as java.util.Map.Entry<*, *>
                        val var16: Any? = (var15.value as WeakReference<*>).get()
                        if (var16 != null && var22.get(var16) == this) {
                            if (mRealApplication != null) {
                                var22.set(var16, mRealApplication)
                            }

                            if (mRealApplication != null && var23 != null) {
                                var23.set(mRealApplication, var16)
                            }
                        }
                    }
                }
            } catch (e: Throwable) {
                Log.e(Constants.TAG, "Error, reflect Application fail, result:$e")
                isReflectFailure = true
            }

            if (!this.isReflectFailure) {
                try {
                    val applicationLikeClass: Class<*> = Class.forName(
                        "com.melancholy.hot.update.tinker.TinkerApplicationLike",
                        false, classLoader)
                    Log.e(Constants.TAG, "replaceApplicationLike delegateClass:$applicationLikeClass")
                    val currentActivityThread: Any = applicationLikeClass
                        .getDeclaredMethod("getTinkerApplicationLike").invoke(applicationLikeClass)!!
                    Utils.findField(applicationLikeClass, "application")
                        .set(currentActivityThread, mRealApplication)
                } catch (e: Throwable) {
                    Log.e(Constants.TAG, "replaceApplicationLike exception:${e.message}")
                }
            }
        }

        super.onCreate()
        mRealApplication?.onCreate()
        Log.d("TAG123456", "执行了TinkerApplication的onCreate方法")
    }

    */
/**
     * 获取真实的application全类名
     * @param context
     *//*

    private fun getRawApplicationName(context: Context): String? {
        if (mRawApplicationName != null) {
            return mRawApplicationName
        } else {
            try {
                mRawApplicationName = context.packageManager.getApplicationInfo(context.packageName,
                    PackageManager.GET_META_DATA).metaData.getString(Constants.REAL_APPLICATION_NAME)
            } catch (e: Exception) {
                Log.e(Constants.TAG, "getManifestApplication exception:${e.message}")
                return null
            }
            return mRawApplicationName
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (isReflectFailure && mRealApplication != null) {
            mRealApplication!!.onConfigurationChanged(newConfig)
        } else {
            super.onConfigurationChanged(newConfig)
        }
    }

    override fun onLowMemory() {
        if (isReflectFailure && mRealApplication != null) {
            mRealApplication!!.onLowMemory()
        } else {
            super.onLowMemory()
        }
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    override fun onTrimMemory(level: Int) {
        if (isReflectFailure && mRealApplication != null) {
            mRealApplication!!.onTrimMemory(level)
        } else {
            super.onTrimMemory(level)
        }
    }

    override fun onTerminate() {
        if (isReflectFailure && mRealApplication != null) {
            mRealApplication!!.onTerminate()
        } else {
            super.onTerminate()
        }
    }

    override fun registerReceiver(receiver: BroadcastReceiver?, filter: IntentFilter?): Intent? {
        return if (isReflectFailure && mRealApplication != null) {
            mRealApplication!!.registerReceiver(receiver, filter)
        } else {
            super.registerReceiver(receiver, filter)
        }
    }

    override fun unregisterReceiver(receiver: BroadcastReceiver?) {
        if (isReflectFailure && mRealApplication != null) {
            mRealApplication!!.unregisterReceiver(receiver)
        } else {
            super.unregisterReceiver(receiver)
        }
    }

    override fun bindService(service: Intent?, conn: ServiceConnection, flags: Int): Boolean {
        return if (isReflectFailure && mRealApplication != null) {
            mRealApplication!!.bindService(service, conn, flags)
        } else {
            super.bindService(service, conn, flags)
        }
    }

    override fun unbindService(conn: ServiceConnection) {
        if (isReflectFailure && mRealApplication != null) {
            mRealApplication!!.unbindService(conn)
        } else {
            super.unbindService(conn)
        }
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    override fun registerComponentCallbacks(var1: ComponentCallbacks?) {
        if (isReflectFailure && mRealApplication != null) {
            mRealApplication!!.registerComponentCallbacks(var1)
        } else {
            super.registerComponentCallbacks(var1)
        }
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    override fun unregisterComponentCallbacks(var1: ComponentCallbacks?) {
        if (isReflectFailure && mRealApplication != null) {
            mRealApplication!!.unregisterComponentCallbacks(var1)
        } else {
            super.unregisterComponentCallbacks(var1)
        }
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    override fun registerActivityLifecycleCallbacks(var1: ActivityLifecycleCallbacks?) {
        if (isReflectFailure && mRealApplication != null) {
            mRealApplication!!.registerActivityLifecycleCallbacks(var1)
        } else {
            super.registerActivityLifecycleCallbacks(var1)
        }
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    override fun unregisterActivityLifecycleCallbacks(var1: ActivityLifecycleCallbacks?) {
        if (isReflectFailure && mRealApplication != null) {
            mRealApplication!!.unregisterActivityLifecycleCallbacks(var1)
        } else {
            super.unregisterActivityLifecycleCallbacks(var1)
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    override fun registerOnProvideAssistDataListener(var1: OnProvideAssistDataListener?) {
        if (isReflectFailure && mRealApplication != null) {
            mRealApplication!!.registerOnProvideAssistDataListener(var1)
        } else {
            super.registerOnProvideAssistDataListener(var1)
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    override fun unregisterOnProvideAssistDataListener(var1: OnProvideAssistDataListener?) {
        if (isReflectFailure && mRealApplication != null) {
            mRealApplication!!.unregisterOnProvideAssistDataListener(var1)
        } else {
            super.unregisterOnProvideAssistDataListener(var1)
        }
    }

    override fun getResources(): Resources? {
        return if (isReflectFailure && mRealApplication != null) {
            mRealApplication!!.resources
        } else {
            super.getResources()
        }
    }

    override fun getClassLoader(): ClassLoader? {
        return if (isReflectFailure && mRealApplication != null) {
            mRealApplication!!.classLoader
        } else {
            super.getClassLoader()
        }
    }

    override fun getAssets(): AssetManager? {
        return if (isReflectFailure && mRealApplication != null) {
            mRealApplication!!.assets
        } else {
            super.getAssets()
        }
    }

    override fun getContentResolver(): ContentResolver? {
        return if (isReflectFailure && mRealApplication != null) {
            mRealApplication!!.contentResolver
        } else {
            super.getContentResolver()
        }
    }

}*/
