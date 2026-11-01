package com.owen.stocksample.dialog

import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResult
import com.owen.stocksample.R
import com.owen.stocksample.databinding.DialogSimpleCenterBinding
import com.owen.stocksample.dialog.SimpleCenterDialog.Companion.RESULT_IS_POSITIVE

/**
 * 顯示標題、訊息與確定 / 取消按鈕的置中 dialog。
 * 點擊按鈕後以 Fragment Result 回傳 [RESULT_IS_POSITIVE]，旋轉螢幕後仍能收到結果；點擊外部或返回鍵關閉時不回傳。
 */
class SimpleCenterDialog : BaseCenterDialog<DialogSimpleCenterBinding>() {

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?) =
        DialogSimpleCenterBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val args = requireArguments()
        val title = args.getString(ARG_TITLE)
        val negativeText = args.getString(ARG_NEGATIVE_TEXT)

        with(binding) {
            tvTitle.isVisible = title != null
            tvTitle.text = title
            tvMsg.text = args.getString(ARG_MESSAGE)
            Log.d(TAG, "onViewCreated: tvMsg.text: ${tvMsg.text}")
            
            btnPositive.text = args.getString(ARG_POSITIVE_TEXT) ?: getString(R.string.common_confirm)
            btnPositive.setOnClickListener {
                sendResult()
            }

            btnNegative.isVisible = negativeText != null
            btnNegative.text = negativeText
            btnNegative.setOnClickListener { dismiss() }
        }
    }

    private fun sendResult() {
        val result = requireArguments().apply { putBoolean(RESULT_IS_POSITIVE, true) }
        setFragmentResult(requireArguments().getString(ARG_REQUEST_KEY)!!, result)
        dismiss()
    }

    companion object {
        const val TAG = "SimpleCenterDialog"

        /** 結果 Bundle 的 key，值為 Boolean：true 為確定，false 為取消 */
        const val RESULT_IS_POSITIVE = "result_is_positive"

        private const val ARG_REQUEST_KEY = "arg_request_key"
        private const val ARG_TITLE = "arg_title"
        private const val ARG_MESSAGE = "arg_message"
        private const val ARG_POSITIVE_TEXT = "arg_positive_text"
        private const val ARG_NEGATIVE_TEXT = "arg_negative_text"

        private const val ARG_DATA = "arg_data"

        /**
         * 建立 dialog。
         * [requestKey] 為接收結果時 setFragmentResultListener 使用的 key；[title]、[negativeText] 為 null 時隱藏，[positiveText] 為 null 時顯示「確定」。
         */
        fun newInstance(
            requestKey: String,
            message: String,
            title: String? = null,
            data: Parcelable? = null,
            positiveText: String? = null,
            negativeText: String? = null,
        ) = SimpleCenterDialog().apply {
            arguments = Bundle().apply {
                putString(ARG_REQUEST_KEY, requestKey)
                putString(ARG_TITLE, title)
                putString(ARG_MESSAGE, message)
                putParcelable(ARG_DATA, data)
                putString(ARG_POSITIVE_TEXT, positiveText)
                putString(ARG_NEGATIVE_TEXT, negativeText)
            }
        }
    }
}
