package com.melancholy.hot.update.tinker

/**
 * @author wr
 * @create_time 21-5-7 下午2:26
 * @description j
 */
interface TinkerListener {

    /**
     * 修复包成功
     */
    fun onApplySuccess()

    /**
     * 修复包失败
     */
    fun onApplyFailure()
}