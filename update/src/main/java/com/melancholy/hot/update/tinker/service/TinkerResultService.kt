package com.melancholy.hot.update.tinker.service

import android.annotation.SuppressLint
import androidx.arch.core.executor.ArchTaskExecutor
import com.melancholy.hot.update.tinker.TinkerManager
import com.tencent.tinker.lib.service.AbstractResultService
import com.tencent.tinker.lib.service.PatchResult
import com.tencent.tinker.loader.shareutil.ShareConstants
import com.tencent.tinker.loader.shareutil.SharePatchFileUtil
import java.io.File

/**
 * 用来接收tinker修复包的成功和失败
 *
 */
class TinkerResultService: AbstractResultService() {

    @SuppressLint("RestrictedApi")
    override fun onPatchResult(result: PatchResult?) {
        if(result == null) {
            ArchTaskExecutor.getMainThreadExecutor().execute {
                TinkerManager.getTinkerListener()?.onApplyFailure()
            }
            return
        }
        if(result.isSuccess) {
            //删除修复包
            deleteRawPatchFile(File(result.rawPatchFilePath))
            ArchTaskExecutor.getMainThreadExecutor().execute {
                TinkerManager.getTinkerListener()?.onApplySuccess()
            }
        } else {
            ArchTaskExecutor.getMainThreadExecutor().execute {
                TinkerManager.getTinkerListener()?.onApplyFailure()
            }
        }
    }

    /**
     * 删除修复包原生文件
     * @param rawFile
     */
    private fun deleteRawPatchFile(rawFile: File) {
        if (!SharePatchFileUtil.isLegalFile(rawFile)) {
            return
        }
        val fileName = rawFile.name
        if (!fileName.startsWith(ShareConstants.PATCH_BASE_NAME)
            || !fileName.endsWith(ShareConstants.PATCH_SUFFIX)) {
            SharePatchFileUtil.safeDeleteFile(rawFile)
            return
        }
        val parentFile: File = rawFile.parentFile ?: return
        if (!parentFile.name.startsWith(ShareConstants.PATCH_BASE_NAME)) {
            SharePatchFileUtil.safeDeleteFile(rawFile)
        } else {
            val grandFile: File = parentFile.parentFile ?: return
            if (grandFile.name != ShareConstants.PATCH_DIRECTORY_NAME
                && grandFile.name != ShareConstants.PATCH_DIRECTORY_NAME_SPEC
            ) {
                SharePatchFileUtil.safeDeleteFile(rawFile)
            }
        }
    }
}