package com.example.test2

import android.app.DatePickerDialog
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.Button
import com.google.android.material.textfield.TextInputEditText
import android.content.Intent
import com.example.test2.Student2.category.CategoryManagement
import com.example.test2.Student2.task.UpsertTask
import com.example.test2.Student2.task.TaskList
import java.util.Calendar

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Get references to the input fields and button
        val usernameInput = findViewById<TextInputEditText>(R.id.usernameInput)
        val passwordInput = findViewById<TextInputEditText>(R.id.passwordInput)
        val loginButton = findViewById<Button>(R.id.loginButton)

        val registerButton = findViewById<Button>(R.id.registerButton)

        registerButton.setOnClickListener {
            val intent = Intent(this, Register::class.java)
            startActivity(intent)
        }

        val taskButton = findViewById<Button>(R.id.taskButton)

        taskButton.setOnClickListener {
            val intent = Intent(this, UpsertTask::class.java)
            startActivity(intent)
        }

        val taskListButton = findViewById<Button>(R.id.taskList)

        taskListButton.setOnClickListener {
            val intent = Intent(this, TaskList::class.java)
            startActivity(intent)
        }

        // Handle login click
        loginButton.setOnClickListener {
            val username = usernameInput.text.toString()
            val password = passwordInput.text.toString()

            println("Username: $username")
            println("Password: $password")
        }

        val categoryButton = findViewById<Button>(R.id.categoryButton)
        categoryButton.setOnClickListener {
            val intent = Intent(this, CategoryManagement::class.java)
            startActivity(intent)
        }

        /*
        val datePicker = findViewById<Button>(R.id.datePicker)

        datePicker.setOnClickListener {
            val intent = Intent(this, DatePicker::class.java)
            startActivity(intent)
        }

         */
        val btnDate = findViewById<Button>(R.id.datePicker)

        btnDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val dialog = DatePickerDialog(
                this,
                { _, year1, month1, dayOfMonth ->
                    val displayMonth = month1 + 1 // month is 0-based
                    btnDate.text = "$dayOfMonth/$displayMonth/$year1"
                },
                year, month, day
            )
            dialog.show()
        }


    }

}