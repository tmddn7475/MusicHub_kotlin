package com.example.musichub.Activity

import android.app.Activity
import android.app.Dialog
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Bundle
import android.view.Window
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.musichub.Data.AccountData
import com.example.musichub.R
import com.example.musichub.databinding.ActivityRegisterBinding
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    lateinit var dialog: Dialog
    private var image: Uri? = null
    private var byteArray: ByteArray? = null
    var imageUrl: String = ""
    var email: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        email = FirebaseAuth.getInstance().currentUser?.email.toString()

        dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.progress_layout2)
        dialog.setCancelable(false)

        binding.registerImageBtn.setOnClickListener{
            ImagePicker.with(this)
                .crop(1f, 1f).compress(1024)
                .maxResultSize(640, 640)
                .createIntent { intent -> imageLauncher.launch(intent) }
        }

        binding.registerBtn.setOnClickListener{
            val email:String = binding.registerEmail.text.toString()
            val password:String = binding.registerPwd.text.toString()
            val nickname:String = binding.registerNickname.text.toString()

            if(email.isEmpty() or password.isEmpty() or nickname.isEmpty()){
                Toast.makeText(this, getString(R.string.enter_all), Toast.LENGTH_SHORT).show()
            } else if(password.length < 6) {
                Toast.makeText(this, getString(R.string.password_6), Toast.LENGTH_SHORT).show()
            } else {
                dialog.show()
                FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { p0 ->
                        if (p0.isComplete) {
                            uploadImageToServer(byteArray, email, password, nickname)
                        } else {
                            Toast.makeText(this@RegisterActivity, getString(R.string.try_again), Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }

        binding.registerBackBtn.setOnClickListener{
            finish()
        }
    }

    private fun uploadImageToServer(byteArray: ByteArray?, email:String, pwd:String, name:String){
        if(byteArray == null){
            addAccount(email, pwd, name, "")
        } else {
            FirebaseStorage.getInstance().getReference().child("Accounts_Thumbnails")
                .child(email).putBytes(byteArray).addOnSuccessListener { p0 ->
                val task: Task<Uri> = p0!!.storage.downloadUrl
                while (!task.isComplete);
                val urlSong = task.result
                addAccount(email, pwd, name, urlSong.toString())
            }.addOnFailureListener {
                Toast.makeText(this, "error", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
        }
    }

    private fun addAccount(email: String, password: String, nickname: String, imageUrl:String){
        val accountData = AccountData(email = email, password = password, nickname = nickname, imageUrl = imageUrl, info = "")
        FirebaseDatabase.getInstance().getReference("accounts").push().setValue(accountData)
            .addOnCompleteListener {
                Toast.makeText(this@RegisterActivity, getString(R.string.sign_up_complete), Toast.LENGTH_SHORT).show()
                dialog.dismiss()
                finish()
            }
    }

    private val imageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result: ActivityResult ->
        val resultCode = result.resultCode
        val data = result.data

        if (resultCode == Activity.RESULT_OK) {
            image = data?.data
            // Uri를 활용하여 ImageView에 가져온 이미지 표시
            val bitmap = ImageDecoder.decodeBitmap(ImageDecoder.createSource(contentResolver, image!!))
            binding.registerImage.setImageBitmap(bitmap)
            val byteArrayOutputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
            byteArray = byteArrayOutputStream.toByteArray()
        } else {
            image = null
            byteArray = null
        }
    }
}