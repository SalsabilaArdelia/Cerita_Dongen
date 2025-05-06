package com.example.cerita_dongeng.activities

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.cerita_dongeng.R
import com.example.cerita_dongeng.model.ModelMain
import com.example.cerita_dongeng.utils.FirebaseHelper

class AddActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var etTitle: EditText
    private lateinit var etFile: EditText
    private lateinit var txtPrimary: TextView
    private lateinit var btnSubmit: Button
    private lateinit var btnChooseImage: Button
    private lateinit var imageView: ImageView
    private lateinit var progressBar: ProgressBar

    private val db = FirebaseHelper()
    private var modelMain: ModelMain? = null
    private val PICK_IMAGE_REQUEST = 1
    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add)

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        val i = intent
        if (i != null) modelMain = i.getSerializableExtra(DetailActivity.DETAIL_DONGENG) as? ModelMain

        etTitle = findViewById(R.id.editTextTitle)
        etFile = findViewById(R.id.editTextFile)
        btnSubmit = findViewById(R.id.btnSubmit)
        btnChooseImage = findViewById(R.id.btnChooseImage)
        imageView = findViewById(R.id.imageView)
        progressBar = findViewById(R.id.add_progress_bar)
        txtPrimary = findViewById(R.id.txt_primary)

        var editData = false
        if (modelMain != null) {
            etTitle.setText(modelMain?.strJudul)
            etFile.setText(modelMain?.strCerita)
            imageUri = modelMain?.imagePath?.let { Uri.parse(it) }
            imageUri?.let { imageView.setImageURI(it) }
            editData = true
            txtPrimary.text = "Edit Cerita Dongeng"
        }

        btnChooseImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.type = "image/*"
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        btnSubmit.setOnClickListener {
            val textTitle = etTitle.text.toString().trim()
            val textStory = etFile.text.toString().trim()

            if (textTitle.isNotEmpty() && textStory.isNotEmpty()) {
                showLoading(true)
                val imgPath = imageUri?.toString() ?: ""

                if (!editData) {
                    // Simpan gambar juga
                    db.insertDataToFirebase(textTitle, textStory, imgPath) { success, message ->
                        showLoading(false)
                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                        if (success) {
                            startActivity(Intent(this, MainActivity::class.java))
                        }
                    }
                } else {
                    db.editData(modelMain!!.id, textTitle, textStory, imgPath) { success, message ->
                        showLoading(false)
                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                        if (success) {
                            startActivity(Intent(this, MainActivity::class.java))
                        }
                    }
                }
            } else {
                Toast.makeText(this, "Field tidak boleh kosong!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            imageUri = data.data
            imageView.setImageURI(imageUri)
        }
    }

    private fun showLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
