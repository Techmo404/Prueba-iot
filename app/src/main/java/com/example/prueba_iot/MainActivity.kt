package com.example.prueba_iot

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
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

    companion object {
        private const val TAG = "MainActivity"
    }

    // Firebase
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    // Views
    private lateinit var recyclerView: RecyclerView
    private lateinit var todoEditText: EditText
    private lateinit var addButton: Button
    private lateinit var signOutButton: Button

    // UI/State
    private lateinit var todoAdapter: TodoAdapter
    private var todosListener: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Verificar autenticación ANTES de inflar el layout si vas a navegar
        if (auth.currentUser == null) {
            navigateToLogin()
            return
        }

        setContentView(R.layout.activity_main)
        initViews()
        setupRecyclerView()
        setupClickListeners()
        loadTodos()

        Log.d(TAG, "Usuario autenticado: ${auth.currentUser?.email}")
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.recyclerView)
        todoEditText = findViewById(R.id.todoEditText)
        addButton = findViewById(R.id.addButton)
        signOutButton = findViewById(R.id.signOutButton)
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        // El adaptador recibe un callback para marcar completado
        todoAdapter = TodoAdapter { todo, isChecked ->
            updateTodoCompleted(todo, isChecked)
        }
        recyclerView.adapter = todoAdapter
        Log.d(TAG, "RecyclerView configurado")
    }

    private fun setupClickListeners() {
        addButton.setOnClickListener { addNewTodo() }
        signOutButton.setOnClickListener { signOut() }
    }

    private fun addNewTodo() {
        val todoText = todoEditText.text.toString().trim()
        if (todoText.isEmpty()) {
            Toast.makeText(this, "Por favor ingresa una tarea", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = auth.currentUser?.uid ?: return

        val todo = hashMapOf(
            "text" to todoText,
            "completed" to false,
            "userId" to userId,
            "createdAt" to FieldValue.serverTimestamp()
        )

        Log.d(TAG, "Agregando nuevo todo: $todoText")

        db.collection("todos")
            .add(todo)
            .addOnSuccessListener { documentReference ->
                Log.d(TAG, "Todo agregado exitosamente con ID: ${documentReference.id}")
                todoEditText.text.clear()
                Toast.makeText(this, "Tarea agregada", Toast.LENGTH_SHORT).show()
                // Crear un item local con el ID retornado para permitir actualizarlo de inmediato
                val localTodo = Todo(
                    id = documentReference.id,
                    text = todoText,
                    completed = false,
                    userId = userId,
                    createdAt = null
                )
                // Añadir al adaptador para que el usuario pueda interactuar (ej. marcar completado)
                todoAdapter.addTodo(localTodo)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error al agregar todo", e)
                Toast.makeText(this, "Error al agregar tarea: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateTodoCompleted(todo: Todo, isCompleted: Boolean) {
        if (todo.id.isEmpty()) {
            Log.e(TAG, "Error: ID del todo está vacío")
            return
        }

        Log.d(TAG, "Actualizando todo ${todo.id}: completed=$isCompleted")

        db.collection("todos")
            .document(todo.id)
            .update("completed", isCompleted)
            .addOnSuccessListener {
                Log.d(TAG, "Todo actualizado exitosamente")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error al actualizar todo", e)
                Toast.makeText(this, "Error al actualizar: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadTodos() {
        val userId = auth.currentUser?.uid ?: return
        Log.d(TAG, "Cargando todos para usuario: $userId")

        // Remover listener anterior si existe
        todosListener?.remove()

        todosListener = db.collection("todos")
            .whereEqualTo("userId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Log.e(TAG, "Error al escuchar cambios", error)
                    Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                if (snapshots == null) {
                    Log.w(TAG, "Snapshots es null")
                    return@addSnapshotListener
                }

                val todoList = snapshots.documents.mapNotNull { doc ->
                    try {
                        doc.toObject(Todo::class.java)?.copy(id = doc.id)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error al parsear documento ${doc.id}", e)
                        null
                    }
                }

                Log.d(TAG, "Todos cargados: ${todoList.size} items")
                // Actualizar adaptador
                todoAdapter.updateTodos(todoList)

                if (todoList.isEmpty()) {
                    Toast.makeText(this, "No hay tareas. ¡Agrega una!", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun signOut() {
        Log.d(TAG, "Cerrando sesión")
        todosListener?.remove()
        auth.signOut()
        navigateToLogin()
    }

    private fun navigateToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        todosListener?.remove()
        todosListener = null
    }
}
