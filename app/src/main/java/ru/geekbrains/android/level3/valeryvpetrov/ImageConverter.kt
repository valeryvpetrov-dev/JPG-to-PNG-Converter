package ru.geekbrains.android.level3.valeryvpetrov

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import io.reactivex.Single
import io.reactivex.SingleOnSubscribe
import java.io.FileOutputStream

object ImageConverter {

    fun convertJpgToPng(bitmap: Bitmap, pathToBitmap: String): Single<Pair<String, Bitmap>> {
        val (pathImagePickedDir, nameImagePicked) = splitPathToBitmap(pathToBitmap)
        return Single.create(SingleOnSubscribe<Pair<String, Bitmap>> {
            if (it.isDisposed) return@SingleOnSubscribe

            val pathImageOutput = "$pathImagePickedDir/$nameImagePicked.png"
            val imageOutputStream = FileOutputStream(pathImageOutput)
            if (bitmap.compress(Bitmap.CompressFormat.PNG, 100, imageOutputStream))
                it.onSuccess(pathImageOutput to BitmapFactory.decodeFile(pathImageOutput))
            else
                it.onError(Exception("Conversion problem"))

        })
    }

    private fun splitPathToBitmap(pathToBitmap: String): Pair<String, String> {
        val pathImagePickedParts = pathToBitmap.split("/")
        val pathImagePickedDir = pathImagePickedParts
            .subList(1, pathImagePickedParts.size - 1)
            .joinToString(prefix = "/", separator = "/")
        val nameImagePicked = pathImagePickedParts[pathImagePickedParts.size - 1]
            .split(".")[0]

        return pathImagePickedDir to nameImagePicked
    }
}