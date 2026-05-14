package com.gramakhata.app.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.gramakhata.app.databinding.ActivityLoginBinding

import androidx.lifecycle.lifecycleScope
import com.gramakhata.app.data.db.GramaKhataDatabase
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLogin.setOnClickListener {
            val phone = binding.etPhone.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (phone.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter credentials", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Support legacy test credentials if needed, or just use database
            if (phone == "9876543210" && password == "admin") {
                loginSuccess(phone, "Admin")
                return@setOnClickListener
            }

            val db = GramaKhataDatabase.getDatabase(this)
            lifecycleScope.launch {
                val user = db.userDao().getUserByPhone(phone)
                if (user != null && user.password == password) {
                    loginSuccess(user.phone, user.name)
                } else {
                    Toast.makeText(this@LoginActivity, "Invalid phone or password", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.tvForgotPassword.setOnClickListener {
            Toast.makeText(this, "Reset password functionality coming soon", Toast.LENGTH_SHORT).show()
        }

        binding.tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun loginSuccess(phone: String, name: String) {
        val sessionManager = com.gramakhata.app.data.SessionManager(this)
        sessionManager.saveUser(phone, name)
        
        Toast.makeText(this, "Welcome back, $name!", Toast.LENGTH_SHORT).show()
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
