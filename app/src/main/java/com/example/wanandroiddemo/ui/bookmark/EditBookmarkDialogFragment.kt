package com.example.wanandroiddemo.ui.bookmark

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.core.os.BundleCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.wanandroiddemo.data.model.domain.Bookmark
import com.example.wanandroiddemo.databinding.DialogEditBookmarkBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

class EditBookmarkDialogFragment : DialogFragment() {
    private var bookmark: Bookmark? = null
    private var _binding: DialogEditBookmarkBinding? = null
    private val binding get() = _binding!!
    private val viewModel: BookmarkViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { bundle ->
            bookmark = BundleCompat.getParcelable(bundle, KEY_BOOK_MARK, Bookmark::class.java)
        }
        initData()
    }


    private fun initData() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiEvent.collect {
                    if (it is BookMarkUiEvent.DismissDialog) dismiss()
                }
            }
        }

    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogEditBookmarkBinding.inflate(layoutInflater)
        initDialogState()
        return MaterialAlertDialogBuilder(requireContext())
            .setView(binding.root)
            .setPositiveButton("确定", null)
            .setNegativeButton("取消", null)
            .create()
    }

    override fun onStart() {
        super.onStart()
        //  只有在这里，dialog 的视图才已经绘制完毕
        val dialog = dialog as? AlertDialog ?: return
        //  此时 getButton 保证拿到的绝对不为 null，安全接管点击！
        val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
        positiveButton.setOnClickListener {
            submitData()
        }
    }

    private fun submitData() {
        val name = binding.etName.text.toString().trim()
        val link = binding.etLink.text.toString().trim()


        val curBookmark = bookmark
        if (curBookmark != null) {
            viewModel.updateBookmark(curBookmark.id, name, link)
        } else {
            viewModel.addBookmark(name, link)
        }
    }

    private fun initDialogState() {
        val curBookmark = bookmark
        val isEdit = curBookmark != null
        if (isEdit) {
            binding.tvDialogTitle.text = "编辑书签"
            binding.etName.setText(curBookmark.name)
            binding.etLink.setText(curBookmark.link)
        } else {
            binding.tvDialogTitle.text = "添加书签"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val KEY_BOOK_MARK = "key_book_mark"

        fun newInstance(bookmark: Bookmark?): EditBookmarkDialogFragment {
            return EditBookmarkDialogFragment().apply {
                arguments = Bundle().apply { putParcelable(KEY_BOOK_MARK, bookmark) }
            }
        }
    }
}