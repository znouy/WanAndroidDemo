package com.example.wanandroiddemo.ui.todo


import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import androidx.core.os.BundleCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.example.wanandroiddemo.data.model.domain.Todo
import com.example.wanandroiddemo.data.model.dto.TodoPriority
import com.example.wanandroiddemo.databinding.DialogEditTodoBinding
import com.example.wanandroiddemo.util.formatYearMonthDay
import com.example.wanandroiddemo.util.toFormattedDateString
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.util.Calendar

class EditTodoDialogFragment : DialogFragment() {

    //  直接共享宿主 ViewModel，省去一切复杂的 interface Callback 回调
    private val viewModel: TodoViewModel by activityViewModels()

    private var _binding: DialogEditTodoBinding? = null
    private val binding get() = _binding!!

    private var todo: Todo? = null
    private var selectedDateStr = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        todo = arguments?.let { bundle ->
            BundleCompat.getParcelable(bundle, KEY_TODO, Todo::class.java)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogEditTodoBinding.inflate(layoutInflater)

        initDialogState()
        setupListeners()

        return MaterialAlertDialogBuilder(requireContext())
            .setView(binding.root)
            .setPositiveButton("确定") { _, _ ->
                submitData() // 2. View 仅负责“搬运”原始输入值，不负责校验
            }
            .setNegativeButton("取消") { dialog, _ ->
                dialog.dismiss()
            }
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
    private fun initDialogState() {
        val currentTodo = todo
        if (currentTodo != null) {
            binding.tvDialogTitle.text = "编辑任务"
            binding.etTitle.setText(currentTodo.title)
            binding.etContent.setText(currentTodo.content)
            binding.tvDateDisplay.text = currentTodo.dateStr
            selectedDateStr = currentTodo.dateStr
            if (currentTodo.priority == TodoPriority.HIGH) {
                binding.rbHigh.isChecked = true
            } else {
                binding.rbNormal.isChecked = true
            }
        } else {
            binding.tvDialogTitle.text = "创建新任务"

            val c = Calendar.getInstance()
            selectedDateStr = c.toFormattedDateString()

            binding.tvDateDisplay.text = selectedDateStr
        }
    }

    private fun setupListeners() {
        binding.layoutDate.setOnClickListener {
            val c = Calendar.getInstance()
            DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    selectedDateStr = formatYearMonthDay(year, month, dayOfMonth)
                    binding.tvDateDisplay.text = selectedDateStr
                },
                c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun submitData() {
        val title = binding.etTitle.text.toString().trim()
        val content = binding.etContent.text.toString().trim()
        val priorityValue = if (binding.rbHigh.isChecked) 1 else 2

        val currentTodo = todo
        if (currentTodo != null) {
            viewModel.updateTodo(
                id = currentTodo.id,
                title = title,
                content = content,
                date = selectedDateStr,
                isDone = currentTodo.isDone,
                priority = priorityValue
            )
        } else {
            viewModel.addTodo(
                title = title,
                content = content,
                date = selectedDateStr,
                priority = priorityValue
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null //  防泄漏守卫：必须在销毁时手动置空 ViewBinding
    }

    companion object {
        private const val KEY_TODO = "key_todo"

        fun newInstance(todo: Todo?): EditTodoDialogFragment {
            return EditTodoDialogFragment().apply {
                arguments = Bundle().apply { putParcelable(KEY_TODO, todo) }
            }
        }
    }
}