package com.owen.stocksample.stock.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.owen.stocksample.databinding.BottomDialogStockSortBinding
import com.owen.stocksample.databinding.ItemSortOptionBinding

/** 選擇個股列表排序方式的 bottom sheet，目前的排序方式後方顯示勾選，點選後套用並關閉。 */
class BottomDialogStockSort : BottomSheetDialogFragment() {

    private var _binding: BottomDialogStockSortBinding? = null
    private val binding get() = _binding!!

    private val viewModel: StockViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomDialogStockSortBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireDialog() as BottomSheetDialog).behavior.apply {
            state = BottomSheetBehavior.STATE_EXPANDED
            skipCollapsed = true
        }

        val currentSort = viewModel.sort.value

        StockSort.entries.forEach { sort ->
            ItemSortOptionBinding.inflate(layoutInflater, binding.llOptions, true).run {
                val isSelected = sort == currentSort
                tvLabel.setText(sort.labelRes)
                tvLabel.isSelected = isSelected
                ivCheck.isVisible = isSelected
                root.setOnClickListener {
                    viewModel.updateSort(sort)
                    dismiss()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "BottomDialogStockSort"
    }
}
