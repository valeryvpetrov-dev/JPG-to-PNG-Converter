package ru.geekbrains.android.level3.valeryvpetrov

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity(), View.OnClickListener,
    ConversionDialogFragment.OnButtonClickListener {

    private var pathImagePicked: String? = null
    private lateinit var converterDisposable: Disposable
    private lateinit var conversionDialogFragment: ConversionDialogFragment

    companion object {
        const val REQUEST_CODE_GET_CONTENT = 123
        const val REQUEST_CODE_PERMISSION_WRITE_EXTERNAL_STORAGE = 124
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        imagePicked.setOnClickListener(this)
        buttonConvert.setOnClickListener(this)

        requestPermissionWrite()
    }

    override fun onDestroy() {
        converterDisposable.dispose()
        super.onDestroy()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.imagePicked -> pickImage()
            R.id.buttonConvert -> {
                if (pathImagePicked == null) return

                if (checkPermissionWrite()) {
                    convertJpgToPng(
                        (imagePicked.drawable as BitmapDrawable).bitmap,
                        pathImagePicked!!
                    )
                } else {
                    requestPermissionWrite()
                }
            }
        }
    }

    private fun checkPermissionWrite(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissionWrite() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
            REQUEST_CODE_PERMISSION_WRITE_EXTERNAL_STORAGE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        buttonConvert.isEnabled =
            grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
    }

    private fun pickImage() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/jpg"))
        startActivityForResult(
            Intent.createChooser(intent, "Select Picture"),
            REQUEST_CODE_GET_CONTENT
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK &&
            requestCode == REQUEST_CODE_GET_CONTENT &&
            data != null
        ) {
            val imagePickedUri = data.data
            if (imagePickedUri != null) {
                imagePicked.background = null
                imagePicked.setImageURI(imagePickedUri)
                pathImagePicked = getPathFromUri(imagePickedUri)
                textPathImagePicked.text = pathImagePicked
            }
        }
    }

    private fun getPathFromUri(contentUri: Uri): String? {
        var res: String? = null
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = contentResolver.query(contentUri, projection, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst()
            val columnIndex = cursor.getColumnIndex(projection[0])
            columnIndex.let {
                res = cursor.getString(columnIndex)
            }
            cursor.close()
        }
        return res
    }

    private fun convertJpgToPng(imagePicked: Bitmap, pathImagePicked: String) {
        conversionDialogFragment = ConversionDialogFragment(this)
        conversionDialogFragment.show(supportFragmentManager, "conversionDialogTag")

        converterDisposable = ImageConverter.convertJpgToPng(imagePicked, pathImagePicked)
            .cache()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                Toast.makeText(this, "${it.first} converted to png.", Toast.LENGTH_LONG).show()
                textPathImageConverted.text = it.first
                imageConverted.setImageBitmap(it.second)
                conversionDialogFragment.dismiss()
            }, {
                Toast.makeText(this, it.message, Toast.LENGTH_LONG).show()
                conversionDialogFragment.dismiss()
            })
    }

    override fun onPositiveClick() {
        converterDisposable.dispose()
        conversionDialogFragment.dismiss()
    }

}
