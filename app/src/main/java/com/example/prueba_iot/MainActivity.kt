package com.example.prueba_iot

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import android.content.Intent

class MainActivity : AppCompatActivity() {

    private lateinit var objectEditText: EditText
    private lateinit var priceEditText: EditText
    private lateinit var qtyEditText: EditText
    private lateinit var addButton: Button
    private lateinit var recyclerView: RecyclerView

    private lateinit var adapter: TodoAdapter
    private val db = FirebaseFirestore.getInstance()
    private val user = FirebaseAuth.getInstance().currentUser
    private var expenses = mutableListOf<Expense>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        objectEditText = findViewById(R.id.objectEditText)
        priceEditText = findViewById(R.id.priceEditText)
        qtyEditText = findViewById(R.id.qtyEditText)
        addButton = findViewById(R.id.addButton)
        recyclerView = findViewById(R.id.recyclerView)

        adapter = TodoAdapter(expenses)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // 🔹 Botón cerrar sesión correctamente
        findViewById<Button>(R.id.signOutButton).setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        loadExpenses()

        addButton.setOnClickListener {
            addExpense()
        }
    }

    private fun addExpense() {
        val obj = objectEditText.text.toString().trim()
        val rawPrice = priceEditText.text.toString().trim()
        val qty = qtyEditText.text.toString().trim().toIntOrNull() ?: 1

        if (obj.isEmpty() || rawPrice.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        // 🔹 Limpia puntos y comas
        val cleanPrice = rawPrice.replace(".", "").replace(",", "")
        val price = cleanPrice.toDoubleOrNull() ?: 0.0

        val id = db.collection("expenses").document().id

        val expense = Expense(
            id = id,
            `object` = obj,
            price = price,
            qty = qty,
            userId = user!!.uid,
        )

        db.collection("expenses").document(id).set(expense)
            .addOnSuccessListener {
                objectEditText.text.clear()
                priceEditText.text.clear()
                qtyEditText.text.clear()
                Toast.makeText(this, "Agregado!", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadExpenses() {
        db.collection("expenses")
            .whereEqualTo("userId", user?.uid)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    expenses = snapshot.toObjects(Expense::class.java).toMutableList()
                    adapter.update(expenses)
                }
            }
    }
}

