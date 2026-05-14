package com.gramakhata.app.data.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.gramakhata.app.data.model.Customer

@Dao
interface CustomerDao {

    @Query("SELECT * FROM customers WHERE userId = :userId AND isSettled = 0 ORDER BY (totalCredit - totalPaid) DESC")
    fun getAllActiveCustomers(userId: String): LiveData<List<Customer>>

    @Query("SELECT * FROM customers WHERE userId = :userId ORDER BY (totalCredit - totalPaid) DESC")
    fun getAllCustomers(userId: String): LiveData<List<Customer>>

    @Query("SELECT * FROM customers WHERE id = :id")
    fun getCustomerById(id: Long): LiveData<Customer>

    @Query("SELECT * FROM customers WHERE id = :id")
    suspend fun getCustomerByIdSync(id: Long): Customer?

    @Query("SELECT * FROM customers WHERE userId = :userId AND name LIKE '%' || :query || '%' AND isSettled = 0 ORDER BY (totalCredit - totalPaid) DESC")
    fun searchCustomers(userId: String, query: String): LiveData<List<Customer>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(customer: Customer): Long

    @Update
    suspend fun update(customer: Customer)

    @Delete
    suspend fun delete(customer: Customer)

    @Query("SELECT SUM(totalCredit - totalPaid) FROM customers WHERE userId = :userId AND isSettled = 0")
    fun getTotalDues(userId: String): LiveData<Double?>

    @Query("SELECT COUNT(*) FROM customers WHERE userId = :userId AND isSettled = 0 AND (totalCredit - totalPaid) > 0")
    fun getActiveDebtorCount(userId: String): LiveData<Int>

    @Query("DELETE FROM customers WHERE userId = :userId")
    suspend fun deleteCustomersByUserId(userId: String)
}
