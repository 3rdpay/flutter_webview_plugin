package com.flutter_webview_plugin

import android.content.ClipboardManager
import android.content.Context
import android.content.res.TypedArray
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import org.magicalwater.mgkotlin.mgDialogKt.dialog.MGDialogButton
import org.magicalwater.mgkotlin.mgDialogKt.dialog.MGDialogParam
import org.magicalwater.mgkotlin.mgDialogKt.dialog.MGDialogResultDelegateInUtils
import org.magicalwater.mgkotlin.mgDialogKt.dialog.MGDialogType
import org.magicalwater.mgkotlin.mgviewskt.layout.MGBaseLinearLayout
import java.util.*
import kotlin.properties.Delegates
import android.content.Context.CLIPBOARD_SERVICE
import androidx.core.content.ContextCompat.getSystemService
import android.content.ClipData
import android.widget.Toast


/**
 * Created by magicalwater on 2018/1/15.
 * 時間選擇浮出視窗
 */
class WeChatDialog(context: Context, override var delegate: MGDialogResultDelegateInUtils?) : MGBaseLinearLayout(context), MGDialogParam {

    override fun contentLayout(): Int = R.layout.dialog_wechat

    override fun setupWidget(style: TypedArray?) {

    }

    override var dialogType: MGDialogType = 10

    var weChat: String = ""

    init {
        setupWidget(mStyleArray)
        setBackgroundResource(R.drawable.bg_dialog);
        orientation = LinearLayout.VERTICAL

        val checkView = findViewById<TextView>(R.id.check)
        val cancelView = findViewById<TextView>(R.id.cancel)
        val copyView = findViewById<TextView>(R.id.copy)

        checkView.setOnClickListener {
            println("確認跳轉 wechat")
            delegate?.dialogResult(this, MGDialogButton.RIGHT, null)
        }

        cancelView.setOnClickListener {
            println("取消跳轉 wechat")
            delegate?.dialogResult(this, MGDialogButton.LEFT, null)
        }

        copyView.setOnClickListener {
            println("複製跳轉 wechat")
            val manager: ClipboardManager = context.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            val text = weChat
            val myClip = ClipData.newPlainText("weChat", text)
            manager.primaryClip = myClip
            Toast.makeText(context, "已复制微信号到剪贴板", Toast.LENGTH_SHORT).show()

        }
    }

    fun setWeChatId(id: String) {
        val idView = findViewById<TextView>(R.id.weChatId)
        idView.text = id
        weChat = id
    }


}