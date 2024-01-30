package com.cerebus.contentresolverexample

import android.annotation.SuppressLint
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.widget.Button
import android.widget.TextView
import androidx.annotation.WorkerThread

class MainActivity : AppCompatActivity() {

    private lateinit var idTextView: TextView
    private lateinit var nameTextView: TextView
    private lateinit var showNextButton: Button
    private lateinit var showPreviousButton: Button
    private lateinit var showFirstButton: Button

    val handler = Handler(Looper.getMainLooper())
    val uri = Uri.parse("content://com.cerebus.contentprovidersample.MyContentProvider/ExampleTable") //all names
    val projection = arrayOf("id", "name")

    private var counter = 0

    val t = HandlerThread("Db thread")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        t.start()
        setContentView(R.layout.activity_main)

        findViews()
        setClickListeners()
    }

    private fun findViews() {
        idTextView = findViewById(R.id.idTextView)
        nameTextView = findViewById(R.id.nameTextView)
        showNextButton = findViewById(R.id.showNextButton)
        showPreviousButton = findViewById(R.id.showPreviousButton)
        showFirstButton = findViewById(R.id.showFirstButton)
    }

    private fun setClickListeners() {
        val dbHandler = Handler(t.looper)
        showNextButton.setOnClickListener { dbHandler.post { requestNextName() } }
        showPreviousButton.setOnClickListener { dbHandler.post{ requestPreviousName() } }
        showFirstButton.setOnClickListener { dbHandler.post {requestFirstName() }}
    }

    private fun setIdAndName(person: Person) {
        idTextView.text = person.id.toString()
        nameTextView.text = person.name

    }

    @SuppressLint("Range")
    private fun requestNextName(): Person? {
        counter++
        val person: Person? = personQuery(counter)
        if (person != null) {
            person?.let { pers ->
                handler.post { setIdAndName(pers) }
            }
        } else counter--

        return person
    }

    @SuppressLint("Range")
    private fun requestPreviousName(): Person? {
        if (counter > 1) {
            counter--
            val person: Person? = personQuery(counter)


            person?.let { pers ->
                handler.post { setIdAndName(pers) }
            }
            return person
        }
        return null
    }

    @SuppressLint("Range")
    private fun personQuery(counter: Int): Person? {
        var person: Person? = null
        if (counter >= 0) {
            val selection = "id"
            val selectionArgs = arrayOf(counter.toString())

            val cursor = contentResolver.query(uri, projection, selection, selectionArgs, null)
            cursor?.use {
                while (it.moveToNext()) {
                    person = Person(it.getInt(it.getColumnIndex("id")), it.getString(it.getColumnIndex("name")))
                }
            }
        }
        return person
    }

    private fun requestFirstName(): Person? {
        counter = 1
        return personQuery(counter)
    }

    data class Person(val id: Int, val name: String)

    override fun onDestroy() {
        super.onDestroy()
        t.interrupt()
        t.join()
    }
}