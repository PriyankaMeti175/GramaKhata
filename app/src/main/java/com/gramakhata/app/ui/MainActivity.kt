package com.gramakhata.app.ui

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.gramakhata.app.R
import com.gramakhata.app.data.SessionManager
import com.gramakhata.app.data.db.GramaKhataDatabase
import com.gramakhata.app.databinding.ActivityMainBinding
import com.gramakhata.app.ui.customers.AddCustomerActivity
import com.gramakhata.app.ui.customers.CustomerDetailActivity
import com.gramakhata.app.ui.customers.CustomerAdapter
import com.gramakhata.app.viewmodel.KhataViewModel
import java.text.NumberFormat
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: KhataViewModel by viewModels()
    private lateinit var customerAdapter: CustomerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupObservers()
        setupListeners()
    }

    override fun onResume() {
        super.onResume()
        loadUserProfile()
    }

    private fun loadUserProfile() {
        val sessionManager = SessionManager(this)
        val phone = sessionManager.getUserPhone() ?: return
        
        lifecycleScope.launchWhenResumed {
            val db = GramaKhataDatabase.getDatabase(this@MainActivity)
            val user = db.userDao().getUserByPhone(phone)
            
            user?.let {
                binding.tvWelcomeUser.text = "Welcome, ${it.name}"
                
                if (!it.photoUri.isNullOrEmpty()) {
                    Glide.with(this@MainActivity)
                        .load(it.photoUri)
                        .placeholder(R.drawable.ic_person_placeholder)
                        .into(binding.ivUserProfile)
                } else {
                    binding.ivUserProfile.setImageResource(R.drawable.ic_person_placeholder)
                }
            }
        }
    }

    private fun setupRecyclerView() {
        customerAdapter = CustomerAdapter { customer ->
            val intent = Intent(this, CustomerDetailActivity::class.java)
            intent.putExtra("customer_id", customer.id)
            startActivity(intent)
        }
        binding.rvCustomers.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = customerAdapter
        }
    }

    private fun setupObservers() {
        viewModel.displayedCustomers.observe(this) { customers ->
            customerAdapter.submitList(customers)
            binding.tvEmptyState.visibility = if (customers.isEmpty()) View.VISIBLE else View.GONE
            binding.rvCustomers.visibility = if (customers.isEmpty()) View.GONE else View.VISIBLE
        }

        viewModel.totalDues.observe(this) { total ->
            val amount = total ?: 0.0
            binding.tvTotalDue.text = formatCurrency(amount)
        }

        viewModel.activeDebtorCount.observe(this) { count ->
            binding.tvDebtorCount.text = "$count customers"
        }
    }

    private fun setupListeners() {
        binding.fabAddCustomer.setOnClickListener {
            startActivity(Intent(this, AddCustomerActivity::class.java))
        }

        binding.profileSection.setOnClickListener {
            startActivity(Intent(this, AccountSettingsActivity::class.java))
        }

        binding.btnLogout.setOnClickListener {
            startActivity(Intent(this, AccountSettingsActivity::class.java))
        }

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.setSearchQuery(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.btnShareReport.setOnClickListener {
            shareReport()
        }
    }

    private fun shareReport() {
        val customers = viewModel.displayedCustomers.value ?: return
        val total = viewModel.totalDues.value ?: 0.0
        val sb = StringBuilder()
        sb.appendLine("📒 *Grama-Khata Daily Report*")
        sb.appendLine("━━━━━━━━━━━━━━━━━━")
        sb.appendLine()
        customers.filter { it.netDue > 0 }.forEach { c ->
            sb.appendLine("👤 ${c.name}  →  ₹${String.format("%.2f", c.netDue)}")
        }
        sb.appendLine()
        sb.appendLine("━━━━━━━━━━━━━━━━━━")
        sb.appendLine("💰 *Total Due: ₹${String.format("%.2f", total)}*")
        sb.appendLine()
        sb.appendLine("_Sent from Grama-Khata App_")

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, sb.toString())
        }
        startActivity(Intent.createChooser(intent, "Share Report via"))
    }

    private fun formatCurrency(amount: Double): String {
        return "₹${String.format("%,.2f", amount)}"
    }
}
