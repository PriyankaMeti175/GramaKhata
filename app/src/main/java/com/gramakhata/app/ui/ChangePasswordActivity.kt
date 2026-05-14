package com.gramakhata.app.ui

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.gramakhata.app.data.SessionManager
import com.gramakhata.app.data.db.GramaKhataDatabase
import com.gramakhata.app.data.model.User
import com.gramakhata.app.databinding.ActivityChangePasswordBinding
import kotlinx.coroutines.launch

class ChangePasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChangePasswordBinding
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChangePasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        setupToolbar()
        setupListeners()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupListeners() {
        binding.btnUpdatePassword.setOnClickListener {
            updatePassword()
        }
    }

    private fun updatePassword() {
        val currentPwd = binding.etCurrentPassword.text.toString()
        val newPwd = binding.etNewPassword.text.toString()
        val confirmPwd = binding.etConfirmPassword.text.toString()

        if (currentPwd.isEmpty() || newPwd.isEmpty() || confirmPwd.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (newPwd != confirmPwd) {
            binding.etConfirmPassword.error = "Passwords do not match"
            return
        }

        if (newPwd.length < 4) {
            binding.etNewPassword.error = "Password too short"
            return
        }

        val phone = sessionManager.getUserPhone() ?: return

        lifecycleScope.launch {
            val db = GramaKhataDatabase.getDatabase(this@ChangePasswordActivity)
            val user = db.userDao().getUserByPhone(phone)

            if (user != null) {
                if (user.password == currentPwd) {
                    val updatedUser = user.copy(password = newPwd)
                    db.userDao().update(updatedUser)
                    Toast.makeText(this@ChangePasswordActivity, "Password updated successfully", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    binding.etCurrentPassword.error = "Incorrect current password"
                }
            } else {
                Toast.makeText(this@ChangePasswordActivity, "User not found", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
