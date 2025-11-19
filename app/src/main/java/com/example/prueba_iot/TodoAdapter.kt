package com.example.prueba_iot

import android.graphics.Paint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TodoAdapter(
    private val onTodoChecked: (Todo, Boolean) -> Unit
) : RecyclerView.Adapter<TodoAdapter.TodoViewHolder>() {

    private val todos = mutableListOf<Todo>()

    class TodoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.textView)
        val checkBox: CheckBox = itemView.findViewById(R.id.checkBox)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_todo, parent, false)
        return TodoViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: TodoViewHolder, position: Int) {
        val currentTodo = todos[position]

        holder.textView.text = currentTodo.text
        holder.checkBox.isChecked = currentTodo.completed

        // Aplicar estilo de tachado si está completado
        if (currentTodo.completed) {
            holder.textView.paintFlags = holder.textView.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        } else {
            holder.textView.paintFlags = holder.textView.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
        }

        // Remover listeners previos para evitar duplicados
        holder.checkBox.setOnCheckedChangeListener(null)

        // Configurar nuevo listener
        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked != currentTodo.completed) {
                Log.d("TodoAdapter", "Checkbox changed for ${currentTodo.text}: $isChecked")
                onTodoChecked(currentTodo, isChecked)
            }
        }
    }

    override fun getItemCount(): Int = todos.size

    fun updateTodos(newTodos: List<Todo>) {
        Log.d("TodoAdapter", "Updating todos. New count: ${newTodos.size}")
        todos.clear()
        todos.addAll(newTodos)
        notifyDataSetChanged()
    }

    fun addTodo(todo: Todo) {
        todos.add(0, todo)
        notifyItemInserted(0)
    }

    fun updateTodo(updatedTodo: Todo) {
        val index = todos.indexOfFirst { it.id == updatedTodo.id }
        if (index != -1) {
            todos[index] = updatedTodo
            notifyItemChanged(index)
        }
    }

    fun removeTodo(todoId: String) {
        val index = todos.indexOfFirst { it.id == todoId }
        if (index != -1) {
            todos.removeAt(index)
            notifyItemRemoved(index)
        }
    }
}
