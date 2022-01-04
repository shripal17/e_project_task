package com.e.sampleTask

import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.view.isVisible
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.ImageRequest
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {

  private val prefs by lazy {
    PrefsSingleton.getInstance(this)
  }
  private val volley by lazy {
    VolleySingleton.getInstance(this)
  }

  companion object {
    const val fileNameKey = "fileName"
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    loadImageFromDisk()

    button.setOnClickListener {
      loadImage()
    }
  }

  /**
   * Loads previously stored image from temp files directory if present
   */
  private fun loadImageFromDisk(): Boolean {
    val storedFileName = prefs.getString(fileNameKey)
    storedFileName?.let {
      imageView.setImageURI(File(filesDir.absolutePath + File.separator + it + ".jpg").toUri())
      return true
    }
    return false
  }

  /**
   * Loads image from https://picsum.photos
   */
  private fun loadImage() {
    imageView.isVisible = false
    progressBar.isVisible = true

    volley.addToRequestQueue(object : ImageRequest(
      "https://picsum.photos/${imageView.width}?time=${System.currentTimeMillis()}", // time query parameter is added to skip caching
      object : Response.Listener<Bitmap> {
        override fun onResponse(response: Bitmap?) {
          imageView.isVisible = true
          response?.let { // delete previously loaded images
            filesDir.listFiles()?.forEach { prevFile ->
              prevFile.delete()
            }
            imageView.setImageBitmap(response)
            val fileName = System.currentTimeMillis().toString()
            val file = File(filesDir.absolutePath + File.separator + "$fileName.jpg")
            if (!file.exists()) { // save the newly loaded image as a file
              file.createNewFile()
              val bos = ByteArrayOutputStream()
              it.compress(CompressFormat.JPEG, 95, bos)
              val bitmapData: ByteArray = bos.toByteArray()
              val fos = FileOutputStream(file)
              fos.write(bitmapData)
              fos.flush()
              fos.close()
              prefs.putString(fileNameKey, fileName) // save file name to shared preferences
            }
          }
          progressBar.isVisible = false
        }
      },
      imageView.width, imageView.width, ImageView.ScaleType.CENTER_INSIDE, Bitmap.Config.ALPHA_8,
      object : Response.ErrorListener {
        override fun onErrorResponse(error: VolleyError?) {
          imageView.isVisible = true
          progressBar.isVisible = false
          Snackbar.make(root, R.string.no_network, 3_000).show()
        }
      },
    ) {})
  }
}