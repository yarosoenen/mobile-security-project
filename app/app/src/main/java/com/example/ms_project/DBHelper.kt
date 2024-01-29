package com.example.ms_project

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class MyDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION){

    companion object {
        private const val DATABASE_NAME = "my_database.db"
        private const val DATABASE_VERSION = 1
    }

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL("CREATE TABLE users (id INTEGER PRIMARY KEY, name TEXT, password TEXT)")
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS users")
        onCreate(db)
    }

    class User(val id: Int, val name: String, val password: String)

    fun insertUser(name: String, password: String) {
        val db = writableDatabase

        val values = ContentValues().apply {
            put("name", name)
            put("password", password)
        }
        db.insert("users", null, values)

        db.close()
    }

    @SuppressLint("Recycle")
    fun checkPasswordMatch(name: String, pwd: String): Boolean {
        val db = readableDatabase

        val projection = arrayOf("password")
        val selection = "name = ?"
        val selectionArgs = arrayOf(name)
        val cursor = db.query("users", projection, selection, selectionArgs, null, null, null)

        return if (cursor.moveToFirst()) {
            val passwordFromDb = cursor.getString(cursor.getColumnIndexOrThrow("password"))
            pwd == passwordFromDb
        } else {
            false
        }
    }

    @SuppressLint("Recycle")
    fun checkForUserInUse(name: String): Boolean {
        val db = readableDatabase

        val projection = arrayOf("name")
        val selection = "name = ?"
        val selectionArgs = arrayOf(name)
        val cursor = db.query("users", projection, selection, selectionArgs, null, null, null)

        return if (cursor.moveToFirst()) {
            val nameFromDb = cursor.getString(cursor.getColumnIndexOrThrow("name"))
            name == nameFromDb
        } else {
            false
        }
    }

    fun addCurrentUserToFirebase(name: String, latitude: Double?, longitude: Double?) {
        val db = readableDatabase

        val projection = arrayOf("id", "name", "password")
        val selection = "name = ?"
        val selectionArgs = arrayOf(name)
        val cursor = db.query("users", projection, selection, selectionArgs, null, null, null)

        if (cursor.moveToFirst()) {
            val username = cursor.getString(cursor.getColumnIndexOrThrow("name"))

            val database = FirebaseDatabase.getInstance()

            val ref: DatabaseReference = database.getReference("users/$username")

                val currentUser = hashMapOf(
                    "latitude" to latitude,
                    "longitude" to longitude
                )
            ref.setValue(currentUser)
        }
        cursor.close()
    }
}




