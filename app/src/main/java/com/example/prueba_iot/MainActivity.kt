package com.example.prueba_iot

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var recyclerView: RecyclerView
    private lateinit var todoAdapter: TodoAdapter
    private lateinit var addButton: android.widget.Button
    private lateinit var todoEditText: EditText
    private lateinit var signOutButton: android.widget.Button

    private var todosListener: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        if (auth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recyclerView)
        todoEditText = findViewById(R.id.todoEditText)
        addButton = findViewById(R.id.addButton)
        signOutButton = findViewById(R.id.signOutButton)

        recyclerView.layoutManager = LinearLayoutManager(this)

        todoAdapter = TodoAdapter(
            onTodoChecked = { todo, checked -> updateTodoCompleted(todo, checked) },
            onEditTodo = { todo -> editTodo(todo) },
            onDisableTodo = { todo -> disableTodo(todo) },
            onDeleteTodo = { todo -> deleteTodo(todo) }
        )

        recyclerView.adapter = todoAdapter

        addButton.setOnClickListener { addTodo() }
        signOutButton.setOnClickListener { signOut() }

        loadTodos()
    }

    private fun addTodo() {
        val text = todoEditText.text.toString().trim()
        if (text.isEmpty()) {
            Toast.makeText(this, "Ingresa una tarea", Toast.LENGTH_SHORT).show()
            return
        }

        val todo = hashMapOf(
            "text" to text,
            "completed" to false,
            "enabled" to true,
            "userId" to auth.currentUser!!.uid,
            "createdAt" to FieldValue.serverTimestamp()
        )

        db.collection("todos")
            .add(todo)
            .addOnSuccessListener {
                todoEditText.text.clear()
            }
    }

    private fun updateTodoCompleted(todo: Todo, completed: Boolean) {
        db.collection("todos")
            .document(todo.id)
            .update("completed", completed)
    }

    private fun editTodo(todo: Todo) {
        val input = EditText(this)
        input.setText(todo.text)

        AlertDialog.Builder(this)
            .setTitle("Editar tarea")
            .setView(input)
            .setPositiveButton("Guardar") { _, _ ->
                val newText = input.text.toString().trim()
                db.collection("todos")
                    .document(todo.id)
                    .update("text", newText)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun disableTodo(todo: Todo) {
        db.collection("todos")
            .document(todo.id)
            .update("enabled", false)
    }

    private fun deleteTodo(todo: Todo) {
        db.collection("todos")
            .document(todo.id)
            .delete()
    }

    private fun loadTodos() {
        val uid = auth.currentUser!!.uid

        todosListener?.remove()
        todosListener = db.collection("todos")
            .whereEqualTo("userId", uid)
            .whereEqualTo("enabled", true)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, error ->
                if (error != null) return@addSnapshotListener

                val list = snap!!.documents.map { doc ->
                    doc.toObject(Todo::class.java)!!.copy(id = doc.id)
                }

                todoAdapter.updateTodos(list)
            }
    }

    private fun signOut() {
        todosListener?.remove()
        auth.signOut()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}
