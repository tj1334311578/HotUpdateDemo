package com.melancholy.hot.update.plugin.utils

/**
 * @author wr
 * @create_time 2021/6/24 上午10:57
 * @description 常量
 */
class Constants {
    //插件名称
    static final String PLUGIN_NAME = "UpdatePlugin"
    //日志TAG
    static final String TAG = "Update::Register >>> "
    //开启插件信息日志
    static final String ENABLE_INFO = "Project enable update-plugin plugin"

    //替换application真实的实现类
    static final String REPLACE_APPLICATION_NAME = "com.melancholy.hot.update.tinker.TinkerApplication"

    //meta-data的name值
    static final String META_DATA_NAME = "real_application_name"

    static final String MANIFEST_FILE_NAME = "AndroidManifest.xml"
}