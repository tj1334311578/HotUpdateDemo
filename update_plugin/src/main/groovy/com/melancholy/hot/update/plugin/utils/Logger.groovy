package com.melancholy.hot.update.plugin.utils

import org.gradle.api.Project


/**
 * @author wr
 * @create_time 2021/6/24 上午10:57
 * @description logger日志
 */
class Logger {
    private static org.gradle.api.logging.Logger sLogger

    static void make(Project project) {
        sLogger = project.logger
    }

    static void i(String info) {
        if(info != null && sLogger != null) {
            sLogger.info(Constants.TAG + info)
        }
    }

    static void e(String error) {
        if(error != null && sLogger != null) {
            sLogger.error(Constants.TAG + error)
        }
    }

    static void w(String warning) {
        if(warning != null && sLogger != null) {
            sLogger.warn(Constants.TAG + warning)
        }
    }
}