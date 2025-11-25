package com.example.prueba_iot

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TodoAdapter(private var expenseList: MutableList<Expense>) :
    RecyclerView.Adapter<TodoAdapter.TodoViewHolder>() {

    inner class TodoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val checkBox: CheckBox = itemView.findViewById(R.id.checkBox)
        val objectTextView: TextView = itemView.findViewById(R.id.objectTextView)
        val detailsTextView: TextView = itemView.findViewById(R.id.detailsTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoViewHolder {
        return TodoViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_todo, parent, false)
        )
    }

    override fun onBindViewHolder(holder: TodoViewHolder, position: Int) {
        val item = expenseList[position]

        holder.objectTextView.text = item.`object`
        holder.detailsTextView.text = "Precio: ${item.price} | Cantidad: ${item.qty}"

        // Restaurar estado del texto tachado
        holder.checkBox.isChecked =
            holder.objectTextView.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG != 0

        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            holder.objectTextView.paintFlags =
                if (isChecked) holder.objectTextView.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                else holder.objectTextView.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
        }
    }

    override fun getItemCount() = expenseList.size

    fun update(newList: MutableList<Expense>) {
        expenseList = newList
        notifyDataSetChanged()
    }
}
