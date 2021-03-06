package cn.imgrocket.imgrocket.tool

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import androidx.exifinterface.media.ExifInterface
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*


object BitmapUtil {
    fun random(time: Date) = ("${time.time}${(100000..999999).random()}".hashCode() and Integer.MAX_VALUE).toString()

    fun load(context: Context, uri: Uri): Bitmap? {
        try {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri))
            } else {
                BitmapFactory.decodeStream(context.contentResolver.openInputStream(uri))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    @Throws(IOException::class)
    fun bitmap2FileCache(context: Context, bitmap: Bitmap, quality: Int): File {
        val file = Storage.getStorageDirectory(context, "cache")
        val pic = File(file, random(Date()))
        pic.createNewFile()
        val bos = BufferedOutputStream(FileOutputStream(pic))
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, bos)
        bos.flush()
        bos.close()
        return pic
    }

    private fun load(path: String): Bitmap {
        return BitmapFactory.decodeFile(path)
    }

    /** 从给定的路径加载图片，并指定是否自动旋转方向
     * @param path 图片路径
     * @param adjustOrientation 是否自动旋转方向
     */
    @Deprecated("this method is deprecated")
    fun load(path: String, adjustOrientation: Boolean): Bitmap {
        if (!adjustOrientation) {
            return load(path)
        } else {
            var bm = load(path)
            var digree = 0
            var exif: ExifInterface?
            try {
                exif = ExifInterface(path)
            } catch (e: IOException) {
                e.printStackTrace()
                exif = null
            }

            if (exif != null) {
                // 读取图片中相机方向信息
                // 计算旋转角度
                digree = when (exif.getAttributeInt(
                        ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_UNDEFINED
                )) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> 90
                    ExifInterface.ORIENTATION_ROTATE_180 -> 180
                    ExifInterface.ORIENTATION_ROTATE_270 -> 270
                    else -> 0
                }
            }
            if (digree != 0) {
                // 旋转图片
                val m = Matrix()
                m.postRotate(digree.toFloat())
                bm = Bitmap.createBitmap(
                        bm, 0, 0, bm.width,
                        bm.height, m, true
                )
            }
            return bm
        }
    }

    fun Bitmap.cropCenter(): Bitmap {
        val width = this.width
        val height = this.height
        return if (width > height) {
            Bitmap.createBitmap(this, (width - height) / 2, 0, height, height)
        } else {
            Bitmap.createBitmap(this, 0, (height - width) / 2, width, width)
        }
    }

    fun Bitmap.zoom(newWidth: Int, newHeight: Int): Bitmap {
        val width = width
        val height = height
        val scaleWidth = newWidth / width.toFloat()
        val scaleHeight: Float = newHeight / height.toFloat()
        val matrix = Matrix()
        matrix.postScale(scaleWidth, scaleHeight)
        return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
    }


}