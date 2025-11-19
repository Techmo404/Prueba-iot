package com.example.prueba_iot

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TodoAdapter(
    private val onTodoChecked: (Todo, Boolean) -> Unit,
    private val onEditTodo: (Todo) -> Unit,
    private val onDisableTodo: (Todo) -> Unit,
    private val onDeleteTodo: (Todo) -> Unit
) : RecyclerView.Adapter<TodoAdapter.TodoViewHolder>() {

    private var todos: List<Todo> = emptyList()

    fun updateTodos(newList: List<Todo>) {
        todos = newList
        notifyDataSetChanged()
    }

    inner class TodoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val checkBox: CheckBox = view.findViewById(R.id.checkBox)
        val textView: TextView = view.findViewById(R.id.textView)
        val editButton: ImageButton = view.findViewById(R.id.editButton)
        val disableButton: ImageButton = view.findViewById(R.id.disableButton)
        val deleteButton: ImageButton = view.findViewById(R.id.deleteButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_todo, parent, false)
        return TodoViewHolder(view)
    }

    override fun getItemCount(): Int = todos.size

    override fun onBindViewHolder(holder: TodoViewHolder, position: Int) {
        val currentTodo = todos[position]

        // Texto
        holder.textView.text = currentTodo.text

        // Estado completado
        holder.checkBox.setOnCheckedChangeListener(null)
        holder.checkBox.isChecked = currentTodo.completed
        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            onTodoChecked(currentTodo, isChecked)
        }

        // Si está deshabilitado, ponerlo visualmente gris
        if (!currentTodo.enabled) {
            holder.textView.alpha = 0.4f
            holder.checkBox.isEnabled = false
            holder.editButton.isEnabled = false
        } else {
            holder.textView.alpha = 1f
            holder.checkBox.isEnabled = true
            holder.editButton.isEnabled = true
        }

        // Botones
        holder.editButton.setOnClickListener {
            onEditTodo(currentTodo)
        }

        holder.disableButton.setOnClickListener {
            onDisableTodo(currentTodo)
        }

        holder.deleteButton.setOnClickListener {
            onDeleteTodo(currentTodo)
        }
    }
}
