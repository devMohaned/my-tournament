package com.my.tournament

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.view.Gravity
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.LinearLayout
import android.widget.LinearLayout.VERTICAL
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.my.tournament.draws.CircleImageView
import kotlinx.android.synthetic.main.activity_tournment.*
import java.io.ByteArrayOutputStream
import kotlin.math.roundToInt
import android.graphics.BitmapFactory


class TournmentActivity : AppCompatActivity() {

    // Margins
    val marginTop = 0
    val marginBottom = 0
    val marginLeft = 8
    val marginRight = 8

    // Team Size
    val teamSizes = 16
    // Each Sector
    var eachFactor = teamSizes / 2
    // The Full Height of each sector
    var fullHeight = 0
    // The Full width of each sector
    var fullWidth = 0
    // Number of columns of sector
    var columnsNumbers = 0
    // The width of each column
    var columnWidth = 0
    // the height of each column
    var columnHeight = 0
    // the circle of radius
    var circleRadius = 0f;

    // Global Circle to determine to be changed Circle & Index/Key for shared preference.
    var currentCircle: CircleImageView? = null
    var currentIndex: Int = 1


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tournment)

        root.post({
            // get the full height & width of each sector (to get each column width)
            fullHeight = leftSectorLinearLayout.height
            fullWidth = (leftSectorLinearLayout.width * 0.6).roundToInt()

            // Let's find out  how many columns do we have
            // Initialize the starting column count to be 1
            var widthBeg = 1
            while (widthBeg != eachFactor) {
                // Increase the column by 1
                columnsNumbers++
                // Double the size of previous column
                widthBeg = widthBeg * 2
            }
            // The width of each circle/column
            columnWidth = fullWidth / columnsNumbers
            circleRadius = columnWidth * 0.5f;

            // Make the layout
            makeTheLayout()


        })

        ID_current_right_imageview.setOnClickListener {
            obtainImage(
                ID_current_right_imageview,
                100
            );
        }
        ID_current_left_imageview.setOnClickListener {
            obtainImage(
                ID_current_left_imageview,
                100
            );
        }
    }

    // Make both sectors
    fun makeTheLayout() {
        val begging = 1
        // Make the left sector in descending order
        makeLeftSector(eachFactor)
        // Make the right sector in ascending order
        makeRightSector(begging)
        // Result would be ordered tournment
    }


    // Repeated Recursion which is the key of the shared preference
    var repeatedRightRecursion = 0

    // Making Right Sector @Param colSize, it refer to the size of each column
    fun makeRightSector(colSize: Int) {
        // If the column size is bigger than right factor size, stop recursion
        if (colSize > eachFactor)
            return
        // Calculate the height of each circle of column
        val columnHeight = fullHeight / colSize

        // Vertical Linear Layout to contain our circles vertically, and in center, with margins
        var linearlayout = LinearLayout(this)
        linearlayout.layoutParams = LinearLayout.LayoutParams(columnWidth, MATCH_PARENT)
        linearlayout.gravity = Gravity.CENTER_VERTICAL
        linearlayout.orientation = VERTICAL
        val linearlayoutParams = linearlayout.layoutParams as LinearLayout.LayoutParams
        linearlayoutParams.setMargins(marginLeft, marginTop, marginRight, marginBottom)
        // Start loop until sector is done
        var begIndex = 1
        while (begIndex <= colSize) {
            // Make Circle
            val circle = CircleImageView(this)
            circle.layoutParams = LinearLayout.LayoutParams(columnWidth, columnHeight)
            // Show image in the circle if found
            getBase64ImageFromSharedPreference("" + repeatedRightRecursion, circle)

            // temporary value to store the key of shared preference
            val temp = repeatedRightRecursion
            circle.setOnClickListener {
                currentIndex = temp;
                obtainImage(circle, columnHeight);
            }

            // Add views
            linearlayout.addView(circle);
            begIndex++
            repeatedRightRecursion++
        }
        rightSectorLinearLayout.addView(linearlayout)
        // We've finished a column, try to make another column with half prev column (Winners)
        makeRightSector(colSize * 2)

    }


    // Same as Right except in descending order
    var repeatedLeftRecursion = 1

    fun makeLeftSector(colSize: Int) {
        if (colSize < 1)
            return

        val columnHeight = fullHeight / colSize

        val linearlayout = LinearLayout(this)
        linearlayout.layoutParams = LinearLayout.LayoutParams(columnWidth, MATCH_PARENT)
        linearlayout.gravity = Gravity.CENTER_VERTICAL
        linearlayout.orientation = VERTICAL
        val linearlayoutParams = linearlayout.layoutParams as LinearLayout.LayoutParams
        linearlayoutParams.setMargins(marginLeft, marginTop, marginRight, marginBottom)
        var begIndex = 1;
        while (begIndex <= colSize) {
            val circle = CircleImageView(this)
            circle.layoutParams = LinearLayout.LayoutParams(columnWidth, columnHeight)
            getBase64ImageFromSharedPreference("" + repeatedLeftRecursion, circle)

            val temp = repeatedLeftRecursion
            circle.setOnClickListener {
                currentIndex = temp;
                obtainImage(circle, columnHeight);
            }


            linearlayout.addView(circle)
            begIndex++
            repeatedLeftRecursion++
        }
        leftSectorLinearLayout.addView(linearlayout)
        makeLeftSector(colSize / 2)
        repeatedRightRecursion = repeatedLeftRecursion + 1;
    }

    private fun obtainImage(circle: CircleImageView, columnHeight: Int) {
        this.currentCircle = circle as CircleImageView
        this.columnHeight = columnHeight;
        checkPermission()
    }

    @Throws(IllegalArgumentException::class)
    fun convert(base64Str: String): Bitmap {
        val decodedBytes = Base64.decode(
            base64Str.substring(base64Str.indexOf(",") + 1),
            Base64.DEFAULT
        )

        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    }

    private fun getBase64FromBitmap(bm: Bitmap): String? {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bm.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    private fun addToSharedPreference(key: String, base64String: String?) {
        val sharedPreference = getSharedPreferences("my_shared_pref", Context.MODE_PRIVATE)
        val editor = sharedPreference.edit()
        editor.putString(key, base64String)
        editor.apply();
    }

    private fun getBase64ImageFromSharedPreference(key: String, circle: CircleImageView) {
        val sharedPreference = getSharedPreferences("my_shared_pref", Context.MODE_PRIVATE)
        val base64Value = sharedPreference.getString(key, null)
        if (base64Value != null) {
            val b = convert(base64Value) as Bitmap
            circle.setImageBitmap(b)
        } else {
            circle.setImageResource(R.drawable.ic_person_black_24dp)
        }
    }


    private fun checkPermission() {
        //check runtime permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_DENIED
            ) {
                //permission denied
                val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE);
                //show popup to request runtime permission
                requestPermissions(permissions, PERMISSION_CODE);
            } else {
                //permission already granted
                pickImageFromGallery();
            }
        } else {
            //system OS is < Marshmallow
            pickImageFromGallery();
        }
    }

    private fun pickImageFromGallery() {
        //Intent to pick image
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }

    companion object {
        //image pick code
        private val IMAGE_PICK_CODE = 1000;
        //Permission code
        private val PERMISSION_CODE = 1001;
    }


    //handle requested permission result
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            PERMISSION_CODE -> {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //permission from popup granted
                    pickImageFromGallery()
                } else {
                    //permission from popup denied
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    //handle result of picked image
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_PICK_CODE) {
            val imageUri = data?.data
            try {
                imageUri?.let {
                    if (Build.VERSION.SDK_INT < 28) {
                        val bitmap = MediaStore.Images.Media.getBitmap(
                            this.contentResolver,
                            imageUri
                        )
                        val b = resize(bitmap, columnWidth, columnHeight)
                        currentCircle?.setImageBitmap(b)
                        addToSharedPreference("" + currentIndex, getBase64FromBitmap(b))
                    } else {
                        val source = ImageDecoder.createSource(this.contentResolver, imageUri)
                        val bitmap = ImageDecoder.decodeBitmap(source)
                        val b = resize(bitmap, columnWidth, columnHeight)
                        currentCircle?.setImageBitmap(b)
                        addToSharedPreference("" + currentIndex, getBase64FromBitmap(b))
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun resize(image: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        var image = image
        if (maxHeight > 0 && maxWidth > 0) {
            val width = image.width
            val height = image.height
            val ratioBitmap = width.toFloat() / height.toFloat()
            val ratioMax = maxWidth.toFloat() / maxHeight.toFloat()

            var finalWidth = maxWidth
            var finalHeight = maxHeight
            if (ratioMax > 1) {
                finalWidth = (maxHeight.toFloat() * ratioBitmap).toInt()
            } else {
                finalHeight = (maxWidth.toFloat() / ratioBitmap).toInt()
            }
            image = Bitmap.createScaledBitmap(image, finalWidth, finalHeight, true)
            return image
        } else {
            return image
        }
    }
}
