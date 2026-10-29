package edu.umg.pocketboss.persistencia

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class FinanzasDatabase(context: Context) :
    SQLiteOpenHelper(context, "finanzas.db", null, 1) {

    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE categorias (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nombre TEXT NOT NULL UNIQUE,
                esGasto INTEGER NOT NULL
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE transacciones (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                descripcion TEXT NOT NULL,
                monto INTEGER NOT NULL CHECK (monto > 0),
                categoria INTEGER NOT NULL,
                fecha TEXT NOT NULL,
                FOREIGN KEY (categoria)
                    REFERENCES categorias(id)
                    ON DELETE RESTRICT
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(
        db: SQLiteDatabase,
        oldVersion: Int,
        newVersion: Int
    ) {
        // Aquí agregaremos las migraciones cuando sean necesarias.
    }
}