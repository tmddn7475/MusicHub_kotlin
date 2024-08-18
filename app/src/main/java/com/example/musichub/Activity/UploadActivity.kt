package com.example.musichub.Activity

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.musichub.Data.AccountData
import com.example.musichub.Object.Command
import com.example.musichub.Data.MusicData
import com.example.musichub.R
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

class UploadActivity : AppCompatActivity() {

    var uriSong:Uri? = null
    var image:Uri? = null
    var byteArray: ByteArray? = null

    lateinit var fileName:String
    lateinit var songUrl:String
    lateinit var imageUrl:String
    lateinit var songLength:String
    lateinit var nickName:String

    lateinit var uploadImage:ImageView
    lateinit var uploadSelectSong:ImageView
    lateinit var uploadBackBtn:ImageView
    lateinit var songNameEdit:EditText
    lateinit var songCategoryEdit:EditText
    lateinit var songDescriptionEdit:EditText
    lateinit var descriptionLength:TextView
    lateinit var uploadBtn: Button

    lateinit var dialog: Dialog

    val email:String = FirebaseAuth.getInstance().currentUser?.email.toString()
    private val storageReference = FirebaseStorage.getInstance().getReference()
    private val category_arr:Array<String> = arrayOf("None", "Ambient", "Classical", "Dance & EDM",
        "Disco", "Hip hop", "Jazz", "R&B", "Reggae", "Rock")

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload)

        uploadImage = findViewById(R.id.upload_image)
        uploadSelectSong = findViewById(R.id.selectSongButton)
        uploadBackBtn = findViewById(R.id.upload_back_btn)
        songNameEdit = findViewById(R.id.upload_song_name)
        songCategoryEdit = findViewById(R.id.upload_song_category)
        songDescriptionEdit = findViewById(R.id.upload_song_description)
        descriptionLength = findViewById(R.id.upload_length)
        uploadBtn = findViewById(R.id.upload_btn)

        dialog = Dialog(this@UploadActivity)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.progress_layout)
        dialog.setCancelable(false)

        getNickname(email)

        // 곡 썸네일
        uploadImage.clipToOutline = true
        uploadImage.setOnClickListener{
            ImagePicker.with(this@UploadActivity)
                .crop(1f, 1f).compress(1024)
                .maxResultSize(640, 640)
                .createIntent { intent -> imageLauncher.launch(intent) }
        }

        uploadSelectSong.setOnClickListener{
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.setType("audio/*")
            songLauncher.launch(intent)
        }

        // 곡 설명 터치시 editText가 스크롤 되도록 설정
        songDescriptionEdit.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                if (songDescriptionEdit.hasFocus()) {
                    v!!.parent.requestDisallowInterceptTouchEvent(true)
                    when (event!!.action and MotionEvent.ACTION_MASK) {
                        MotionEvent.ACTION_SCROLL -> {
                            v.parent.requestDisallowInterceptTouchEvent(false)
                            return true
                        }
                    }
                }
                return false
            }
        })

        songDescriptionEdit.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            @SuppressLint("SetTextI18n")
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val length: Int = songDescriptionEdit.text.length
                descriptionLength.text = "$length / 2000"
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // 카테고리
        songCategoryEdit.setOnClickListener{
            AlertDialog.Builder(this@UploadActivity)
                .setItems(category_arr) { dialog, which ->
                    songCategoryEdit.setText(category_arr[which])
                }.show()
        }

        uploadBtn.setOnClickListener{
            if(uriSong == null){
                Toast.makeText(this@UploadActivity, getString(R.string.select_track), Toast.LENGTH_SHORT).show()
            } else if(songNameEdit.text.equals("") and songCategoryEdit.text.equals("") and songDescriptionEdit.text.equals("")) {
                Toast.makeText(this@UploadActivity, getString(R.string.enter_all), Toast.LENGTH_SHORT).show()
            } else if (image == null) {
                Toast.makeText(this@UploadActivity, getString(R.string.select_image), Toast.LENGTH_SHORT).show()
            } else {
                dialog.show()
                fileName = songNameEdit.text.toString()
                val description = songDescriptionEdit.text.toString()
                val category = songCategoryEdit.text.toString()

                uploadImageToServer(byteArray!!, fileName)
                uploadFileToServer(uriSong!!, fileName, description, songLength, category)
            }
        }

        uploadBackBtn.setOnClickListener{
            finish()
        }
    }

    private val songLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.data?.let { uri ->
                uriSong = uri
                fileName = getFileName(uri).toString()
                songNameEdit.setText(fileName)
                songLength = getSongDuration(uri)
            }
        } else {
            uriSong = null
        }
    }

    private val imageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result: ActivityResult ->
        val resultCode = result.resultCode
        val data = result.data

        if (resultCode == Activity.RESULT_OK) {
            image = data?.data
            // Uri를 활용하여 ImageView에 가져온 이미지 표시
            val bitmap = ImageDecoder.decodeBitmap(ImageDecoder.createSource(contentResolver, image!!))
            uploadImage.setImageBitmap(bitmap)
            val byteArrayOutputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
            byteArray = byteArrayOutputStream.toByteArray()
        } else {
            image = null
            byteArray = null
        }
    }

    private fun uploadImageToServer(byteArray: ByteArray, fileName:String){
        val uploadTask = storageReference.child("Song_Thumbnails").child("$email/${fileName}_${Command.getTime3()}").putBytes(byteArray)
        uploadTask.addOnSuccessListener { p0 ->
            val task: Task<Uri> = p0!!.storage.downloadUrl
            while (!task.isComplete);
            val urlSong = task.result
            imageUrl = urlSong.toString()
        }.addOnFailureListener {
            Toast.makeText(this@UploadActivity, getString(R.string.try_again), Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun uploadFileToServer(uri: Uri, songName:String, description:String, duration:String, category:String){
        val filePath = storageReference.child("Audios").child("$email/${songName}_${Command.getTime3()}")
        filePath.putFile(uri).addOnSuccessListener { p0 ->
            val task: Task<Uri> = p0!!.storage.downloadUrl
            while (!task.isComplete);
            val urlSong = task.result
            songUrl = urlSong.toString()
            uploadDetailsToDatabase(fileName, songUrl, imageUrl, description, duration, category)
        }.addOnProgressListener { p0 ->
            val percent: TextView = dialog.findViewById(R.id.progress_percent)
            val progress: Double = (100.0 * p0.bytesTransferred) / p0.totalByteCount
            val currentProgress: Int = progress.toInt()
            percent.text = "$currentProgress%"
        }.addOnFailureListener{
            Toast.makeText(this@UploadActivity, getString(R.string.try_again), Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }
    }

    private fun uploadDetailsToDatabase(songName:String, songUri: String, imageUri:String, description:String, songDuration:String, songCategory:String){
        val musicData = MusicData(songName = songName, songUrl = songUri, imageUrl = imageUri, songArtist = nickName, email = email,
            songInfo = description, songDuration = songDuration, songCategory = songCategory, time = Command.getTime())

        FirebaseDatabase.getInstance().getReference("Songs").push().setValue(musicData)
            .addOnCompleteListener {
                Toast.makeText(this@UploadActivity, getString(R.string.upload_complete), Toast.LENGTH_SHORT).show()
                dialog.dismiss()
                finish()
            }.addOnFailureListener {
                Toast.makeText(this@UploadActivity, getString(R.string.try_again), Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
    }

    private fun getNickname(email:String){
        FirebaseDatabase.getInstance().getReference("accounts").orderByChild("email").equalTo(email)
            .limitToFirst(1).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for(ds: DataSnapshot in snapshot.children) {
                        val data = ds.getValue<AccountData>()
                        if (data != null) {
                            nickName = data.nickname
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    @SuppressLint("Range")
    private fun getFileName(uri: Uri): String? {
        var result: String? = null
        if (uri.scheme == "content") {
            contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                }
            }
        }
        if (result == null) {
            result = uri.path
            result?.let {
                val cut = it.lastIndexOf('/')
                if (cut != -1) result = it.substring(cut + 1)
            }
        }
        return result
    }

    private fun getSongDuration(song: Uri): String {
        val mediaMetadataRetriever = MediaMetadataRetriever()
        mediaMetadataRetriever.setDataSource(applicationContext, song)
        val durationString = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
        val time = durationString?.toLongOrNull() ?: 0L
        val minutes = TimeUnit.MILLISECONDS.toMinutes(time).toInt()
        val totalSeconds = TimeUnit.MILLISECONDS.toSeconds(time).toInt()
        val seconds = totalSeconds - (minutes * 60)

        return if (seconds.toString().length == 1) {
            "$minutes:0$seconds"
        } else {
            "$minutes:$seconds"
        }
    }
}