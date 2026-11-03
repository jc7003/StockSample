package com.owen.stocksample.dialog.base

import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.DialogFragment
import androidx.viewbinding.ViewBinding
import com.google.android.material.color.MaterialColors
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import com.owen.stocksample.R
import kotlin.math.min

/**
 * 置中 dialog 的基礎類別。
 * 寬度依內容而定，最寬為螢幕寬度扣掉左右各 [R.dimen.dialog_horizontal_margin]；高度依內容而定，最高為螢幕高度的 [MAX_HEIGHT_RATIO]。
 * 寬度在 onStart 時依內容決定，之後內容變寬不會重新計算，子類別應在 onViewCreated 設定好內容。
 * 內容超過最高高度時會被壓縮，子類別需自行在可捲動區塊使用 ScrollView。
 */
abstract class BaseCenterDialog<VB : ViewBinding> : DialogFragment() {

    private var _binding: VB? = null
    protected val binding get() = _binding!!

    /** 建立子類別的內容 ViewBinding。 */
    protected abstract fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?): VB

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 傳 0 會使用 framework 的 android:dialogTheme，而非 App 的 Material3 主題
        setStyle(STYLE_NO_TITLE, R.style.ThemeOverlay_StockSample_CenterDialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        // 使用 inflater 的 context 才會套用 dialog 主題，requireContext() 是 Activity 主題
        val root = MaxHeightFrameLayout(inflater.context).apply {
            maxHeight = (resources.displayMetrics.heightPixels * MAX_HEIGHT_RATIO).toInt()
            background = createBackground(context)
            clipToOutline = true
        }
        _binding = inflateBinding(inflater, root)
        root.addView(binding.root)
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun createBackground(context: Context): MaterialShapeDrawable {
        val cornerRadius = context.resources.getDimension(R.dimen.dialog_corner_radius)
        val shape = ShapeAppearanceModel.builder().setAllCornerSizes(cornerRadius).build()

        return MaterialShapeDrawable(shape).apply {
            fillColor = ColorStateList.valueOf(
                MaterialColors.getColor(context, com.google.android.material.R.attr.colorSurfaceContainerHigh, TAG),
            )
        }
    }

    /** 限制最高高度的 FrameLayout，作為 dialog 內容的外層。 */
    private class MaxHeightFrameLayout(context: Context) : FrameLayout(context) {

        /** 最高高度（px） */
        var maxHeight = Int.MAX_VALUE

        override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
            val limitedHeightSpec = when (MeasureSpec.getMode(heightMeasureSpec)) {
                MeasureSpec.UNSPECIFIED -> MeasureSpec.makeMeasureSpec(maxHeight, MeasureSpec.AT_MOST)
                else -> MeasureSpec.makeMeasureSpec(
                    min(MeasureSpec.getSize(heightMeasureSpec), maxHeight),
                    MeasureSpec.getMode(heightMeasureSpec),
                )
            }
            super.onMeasure(widthMeasureSpec, limitedHeightSpec)
        }
    }

    companion object {
        private const val TAG = "BaseCenterDialog"

        /** 最高高度佔螢幕高度的比例 */
        private const val MAX_HEIGHT_RATIO = 0.5f
    }
}
