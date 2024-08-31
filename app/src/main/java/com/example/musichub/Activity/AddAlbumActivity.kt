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
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.musichub.Object.Command
import com.example.musichub.Data.AccountData
import com.example.musichub.Data.AlbumData
import com.example.musichub.R
import com.example.musichub.databinding.ActivityAddAlbumBinding
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

    lateinit var binding: ActivityAddAlbumBinding
    lateinit var dialog: Dialog
    private var setMode: String = "private"
    private var image: Uri? = null
    private var byteArray: ByteArray? = null
    var nickname: String = ""
    val email:String = FirebaseAuth.getInstance().currentUser?.email.toString()

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddAlbumBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dialog = Dialog(this@AddAlbumActivity)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.progress_layout2)
        dialog.setCancelable(false)

        getAccount()

        binding.listBackBtn.setOnClickListener{
            finish()
        }

        binding.myListSet.setOnCheckedChangeListener{ _, isChecked ->
            setMode = if(isChecked){
                "public"
            } else {
                "private"
            }
        }

        binding.listSelectImage.clipToOutline = true
        binding.listSelectImage.setOnClickListener{
            ImagePicker.with(this@AddAlbumActivity)
                .crop(1f, 1f).compress(1024)
                .maxResultSize(640, 640)
                .createIntent { intent -> imageLauncher.launch(intent) }
        }

        binding.listDescription.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                if (binding.listDescription.hasFocus()) {
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
        binding.listDescription.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            @SuppressLint("SetTextI18n")
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val length: Int = binding.listDescription.text.length
                binding.listDescriptionLength.text = "$length / 2000"
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // 업로드
        binding.listUploadBtn.setOnClickListener{
            if(image == null) {
                Toast.makeText(this@AddAlbumActivity, getString(R.string.select_image), Toast.LENGTH_SHORT).show()
            } else if (binding.listName.text.equals("")){
                Toast.makeText(this@AddAlbumActivity, getString(R.string.enter_album_title), Toast.LENGTH_SHORT).show()
            } else {
                dialog.show()
                uploadImageToServer(byteArray!!, binding.listName.text.toString(), binding.listDescription.text.toString())
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
            createPlayList(fileName, setMode, desc, imageUrl)
        }.addOnFailureListener {
            Toast.makeText(this@AddAlbumActivity, "error", Toast.LENGTH_SHORT).show()
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
                    Toast.makeText(this@AddAlbumActivity, "error", Toast.LENGTH_SHORT).show()
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
            binding.listSelectImage.setImageBitmap(bitmap)
            val byteArrayOutputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
            byteArray = byteArrayOutputStream.toByteArray()
        } else {
            image = null
            byteArray = null
        }
    }
}