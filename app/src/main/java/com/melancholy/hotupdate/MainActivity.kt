package com.melancholy.hotupdate

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.melancholy.hot.update.tinker.TinkerListener
import com.melancholy.hot.update.tinker.TinkerManager
import com.tencent.tinker.lib.tinker.Tinker
import java.io.File

class MainActivity : AppCompatActivity(), View.OnClickListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<View>(R.id.tv_toast).setOnClickListener(this)
        findViewById<View>(R.id.tv_load).setOnClickListener(this)

        Log.d("TAG123456", "TinkerId ${TinkerManager.getTinkerId()} TinkerVersion ${TinkerManager.getTinkerVersion()}")
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.tv_toast -> {
                Tinker.isTinkerInstalled()
                Toast.makeText(this, "修复包", Toast.LENGTH_SHORT).show()
            }
            R.id.tv_load -> {
                val path: String = File(getExternalFilesDir(null), "1234.apk").absolutePath
                Log.d("TAG123456", "修复包路径: $path")
                TinkerManager.onReceiveUpgradePatch(path)
                TinkerManager.setTinkerListener(object: TinkerListener {
                    override fun onApplySuccess() {
                        Log.d("TAG123456", "tinker修复成功")
                    }

                    override fun onApplyFailure() {
                        Log.d("TAG123456", "tinker修复失败")
                    }

                })
            }
        }
    }
}