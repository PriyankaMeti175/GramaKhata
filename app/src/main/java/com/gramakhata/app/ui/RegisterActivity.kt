package com.gramakhata.app.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.gramakhata.app.databinding.ActivityRegisterBinding

import androidx.lifecycle.lifecycleScope
import com.gramakhata.app.data.db.GramaKhataDatabase
import com.gramakhata.app.data.model.User
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnRegister.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val phone = binding.etPhone.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (name.isNotEmpty() && phone.isNotEmpty() && password.isNotEmpty()) {
                val db = GramaKhataDatabase.getDatabase(this)
                val newUser = User(phone, name, password)

                lifecycleScope.launch {
                    val existingUser = db.userDao().getUserByPhone(phone)
                    if (existingUser == null) {
                        db.userDao().insert(newUser)
                        Toast.makeText(this@RegisterActivity, "Registration Successful! Please login.", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this@RegisterActivity, "Phone number already registered!", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }

        binding.tvLogin.setOnClickListener {
            finish()
        }
    }
}
