package com.example.wanandroiddemo.ui.todo

import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.graphics.toColorInt
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.wanandroiddemo.R
import com.example.wanandroiddemo.data.model.domain.Todo
import com.example.wanandroiddemo.databinding.ItemTodoBinding


class TodoAdapter(
    private val onStatusChanged: (Todo) -> Unit,
    private val onDeleteClick: (Todo) -> Unit,
    private val onEditClick: (Todo) -> Unit,
) : ListAdapter<TodoAdapter.TodoItem, TodoAdapter.ViewHolder>(DiffCallback) {
    
    sealed class TodoItem {
        data class DateHeader(val date: String) : TodoItem()
        data class TodoData(val todo: Todo) : TodoItem()
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is TodoItem.DateHeader -> 0
            is TodoItem.TodoData -> 1
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return if (viewType == 0) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_date_header, parent, false)
            HeaderViewHolder(view)
        } else {
            val binding = ItemTodoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            TodoViewHolder(binding, onStatusChanged, onDeleteClick, onEditClick)
        }
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val item = getItem(position)
        if (viewHolder is TodoViewHolder && item is TodoItem.TodoData) {
            viewHolder.bind(item.todo)
            viewHolder.itemView.setOnClickListener {
                onEditClick(item.todo)
            }
        } else if (viewHolder is HeaderViewHolder && item is TodoItem.DateHeader) {
            viewHolder.bind(item.date)
        }
    }

    abstract class ViewHolder(view: android.view.View) : RecyclerView.ViewHolder(view)

    class HeaderViewHolder(view: android.view.View) : ViewHolder(view) {
        fun bind(date: String) {
            (itemView as android.widget.TextView).text = date
        }
    }

    class TodoViewHolder(
        private val binding: ItemTodoBinding,
        private val onStatusChanged: (Todo) -> Unit,
        private val onDeleteClick: (Todo) -> Unit,
        private val onEditClick: (Todo) -> Unit,
    ) : ViewHolder(binding.root) {

        fun bind(todo: Todo) {
            binding.tvTitle.text = todo.title
            binding.tvContent.text = todo.content
            binding.tvPriority.text = todo.priority.label
            val gd = binding.tvPriority.background as GradientDrawable
            gd.setColor(todo.priority.colorHex.toColorInt())

            //状态动作按钮绑定
            if (todo.isDone) {
                binding.btnToggleStatus.text = "还原"
            } else {
                binding.btnToggleStatus.text = "完成"
            }

            binding.btnDelete.setOnClickListener {
                onDeleteClick(todo)
            }
            // 点击“完成”或“还原”
            binding.btnToggleStatus.setOnClickListener {
                onStatusChanged(todo)
            }

        }
    }

    object DiffCallback : DiffUtil.ItemCallback<TodoItem>() {
        override fun areItemsTheSame(p0: TodoItem, p1: TodoItem): Boolean {
            return if (p0 is TodoItem.TodoData && p1 is TodoItem.TodoData) {
                p0.todo.id == p1.todo.id
            } else if (p0 is TodoItem.DateHeader && p1 is TodoItem.DateHeader) {
                p0.date == p1.date
            } else {
                false
            }
        }

        override fun areContentsTheSame(p0: TodoItem, p1: TodoItem): Boolean {
            return p0 == p1
        }
    }

}
