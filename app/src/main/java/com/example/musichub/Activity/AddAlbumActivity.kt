package com.example.musichub.Activity

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.musichub.Object.Command
import com.example.musichub.Data.AccountData
import com.example.musichub.Data.AlbumData
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

@SuppressLint("UseSwitchCompatOrMaterialCode")
class AddAlbumActivity : AppCompatActivity() {

    var set_mode: String = "private"
    var image: Uri? = null
    var byteArray: ByteArray? = null
    var nickname: String = ""

    lateinit var list_back_btn: ImageView
    lateinit var list_selectImage: ImageView
    lateinit var list_name: EditText
    lateinit var list_description: EditText
    lateinit var list_description_length: TextView
    lateinit var my_list_set: Switch
    lateinit var list_upload_btn: Button
    lateinit var dialog: Dialog

    val email:String = FirebaseAuth.getInstance().currentUser?.email.toString()

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_album)

        list_back_btn = findViewById(R.id.list_back_btn)
        list_selectImage = findViewById(R.id.list_selectImage)
        list_name = findViewById(R.id.list_name)
        list_description = findViewById(R.id.list_description)
        list_description_length = findViewById(R.id.list_description_length)
        my_list_set = findViewById(R.id.my_list_set)
        list_upload_btn = findViewById(R.id.list_upload_btn)

        dialog = Dialog(this@AddAlbumActivity)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.progress_layout2)
        dialog.setCancelable(false)

        getAccount()

        list_back_btn.setOnClickListener{
            finish()
        }

        my_list_set.setOnCheckedChangeListener{ buttonView, isChecked ->
            if(isChecked){
                set_mode = "public"
            } else {
                set_mode = "private"
            }
        }

        list_selectImage.clipToOutline = true
        list_selectImage.setOnClickListener{
            ImagePicker.with(this@AddAlbumActivity)
                .crop(1f, 1f).compress(1024)
                .maxResultSize(640, 640)
                .createIntent { intent -> imageLauncher.launch(intent) }
        }

        list_description.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                if (list_description.hasFocus()) {
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
        list_description.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            @SuppressLint("SetTextI18n")
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val length: Int = list_description.text.length
                list_description_length.text = "$length / 2000"
            }
            override fun afterTextChanged(s: Editable?) {}
        })
        // 업로드
        list_upload_btn.setOnClickListener{
            if(image == null) {
                Toast.makeText(this@AddAlbumActivity, "이미지를 선택해주세요", Toast.LENGTH_SHORT).show()
            } else if (list_name.text.equals("")){
                Toast.makeText(this@AddAlbumActivity, "앨범 이름을 적어주세요", Toast.LENGTH_SHORT).show()
            } else {
                dialog.show()
                uploadImageToServer(byteArray!!, list_name.text.toString(), list_description.text.toString())
            }
        }
    }

    private fun uploadImageToServer(image: ByteArray, fileName: String, desc: String){
        val uploadTask = FirebaseStorage.getInstance().getReference().child("PlayLists_Thumbnails").child("$email/${fileName}_${Command.getTime3()}").putBytes(image)
        uploadTask.addOnSuccessListener { p0 ->
            val task: Task<Uri> = p0!!.storage.downloadUrl
            while (!task.isComplete);
            val urlSong = task.result
            val imageUrl = urlSong.toString()
            createPlayList(fileName, set_mode, desc, imageUrl)
        }.addOnFailureListener {
            Toast.makeText(this@AddAlbumActivity, "오류가 발생했습니다! 다시 시도해주세요", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }
    }

    private fun createPlayList(fileName: String, mode:String, desc: String, imageUrl: String){
        val data = AlbumData(listName = fileName, email = email, list_mode = mode,
            nickname = nickname, description = desc, imageUrl = imageUrl)
        FirebaseDatabase.getInstance().getReference("PlayLists").push().setValue(data).addOnCompleteListener{
            dialog.dismiss()
            finish()
        }
    }

    private fun getAccount(){
        FirebaseDatabase.getInstance().getReference("accounts").orderByChild("email").equalTo(email).limitToFirst(1)
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    for(ds: DataSnapshot in snapshot.children){
                        val data = ds.getValue<AccountData>()
                        if (data != null) {
                            nickname = data.nickname
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@AddAlbumActivity, "오류가 발생했습니다! 다시 시도해주세요", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private val imageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result: ActivityResult ->
        val resultCode = result.resultCode
        val data = result.data

        if (resultCode == Activity.RESULT_OK) {
            image = data?.data
            // Uri를 활용하여 ImageView에 가져온 이미지 표시
            val bitmap = ImageDecoder.decodeBitmap(ImageDecoder.createSource(contentResolver, image!!))
            list_selectImage.setImageBitmap(bitmap)
            val byteArrayOutputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
            byteArray = byteArrayOutputStream.toByteArray()
        } else {
            image = null
            byteArray = null
        }
    }
}