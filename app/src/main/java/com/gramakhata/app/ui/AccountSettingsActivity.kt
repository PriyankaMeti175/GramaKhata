package com.gramakhata.app.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.gramakhata.app.R
import com.gramakhata.app.data.SessionManager
import com.gramakhata.app.data.db.GramaKhataDatabase
import com.gramakhata.app.data.model.User
import com.gramakhata.app.databinding.ActivityAccountSettingsBinding
import kotlinx.coroutines.launch

class AccountSettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAccountSettingsBinding
    private lateinit var sessionManager: SessionManager
    private var currentUser: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAccountSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        setupToolbar()
        setupListeners()
    }

    override fun onResume() {
        super.onResume()
        loadUserData()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun loadUserData() {
        val phone = sessionManager.getUserPhone() ?: return
        lifecycleScope.launch {
            val db = GramaKhataDatabase.getDatabase(this@AccountSettingsActivity)
            currentUser = db.userDao().getUserByPhone(phone)
            
            currentUser?.let { user ->
                binding.tvProfileName.text = user.name
                binding.tvProfilePhone.text = user.phone
                
                if (!user.photoUri.isNullOrEmpty()) {
                    Glide.with(this@AccountSettingsActivity)
                        .load(user.photoUri)
                        .placeholder(R.drawable.ic_person_placeholder)
                        .into(binding.ivProfile)
                } else {
                    binding.ivProfile.setImageResource(R.drawable.ic_person_placeholder)
                }
            }
        }
    }

    private fun setupListeners() {
        binding.btnChangePassword.setOnClickListener {
            startActivity(Intent(this, ChangePasswordActivity::class.java))
        }

        binding.btnSwitchAccount.setOnClickListener {
            logout()
        }

        binding.btnLogout.setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle("Log Out")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Log Out") { _, _ -> logout() }
                .setNegativeButton("Cancel", null)
                .show()
        }

        binding.btnDeleteAccount.setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle("Delete Account")
                .setMessage("This will permanently delete your account and all data. Are you sure?")
                .setPositiveButton("Delete") { _, _ -> deleteAccount() }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun logout() {
        sessionManager.logout()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun deleteAccount() {
        val phone = sessionManager.getUserPhone() ?: return
        val db = GramaKhataDatabase.getDatabase(this)
        lifecycleScope.launch {
            db.customerDao().deleteCustomersByUserId(phone)
            db.userDao().deleteUserByPhone(phone)
            sessionManager.logout()
            Toast.makeText(this@AccountSettingsActivity, "Account deleted", Toast.LENGTH_SHORT).show()
            val intent = Intent(this@AccountSettingsActivity, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}
