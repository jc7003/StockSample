package com.owen.stocksample.core.extensions

import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager

/** 顯示 dialog，同 tag 已顯示或 FragmentManager 狀態已儲存時略過，回傳是否有顯示。 */
fun DialogFragment.showSingle(fragmentManager: FragmentManager, tag: String = javaClass.name): Boolean {
    if (fragmentManager.isStateSaved || fragmentManager.findFragmentByTag(tag) != null) {
        return false
    }
    // showNow 同步 commit，連點時下一次 findFragmentByTag 才找得到
    showNow(fragmentManager, tag)
    return true
}
