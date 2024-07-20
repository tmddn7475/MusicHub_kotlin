package com.example.musichub.Activity

import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Bundle
import android.view.Window
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.musichub.Data.AccountData
import com.example.musichub.R
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import de.hdodenhof.circleimageview.CircleImageView
import java.io.ByteArrayOutputStream
import java.io.IOException

class RegisterActivity : AppCompatActivity() {

    var image: Uri? = null
    var byteArray: ByteArray? = null
    var imageUrl: String = ""
    var email: String = ""

    lateinit var register_image: CircleImageView
    lateinit var register_image_btn: Button
    lateinit var register_btn: Button
    lateinit var register_back_btn: Button
    lateinit var register_email: TextView
    lateinit var register_pwd: TextView
    lateinit var register_pwd_check: TextView
    lateinit var register_nickname: TextView

    lateinit var dialog: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        email = FirebaseAuth.getInstance().currentUser?.email.toString()

        register_image = findViewById(R.id.register_image)
        register_image_btn = findViewById(R.id.register_image_btn)
        register_btn = findViewById(R.id.register_btn)
        register_back_btn = findViewById(R.id.register_back_btn)
        register_email = findViewById(R.id.register_email)
        register_pwd = findViewById(R.id.register_pwd)
        register_pwd_check = findViewById(R.id.register_pwd_check)
        register_nickname = findViewById(R.id.register_nickname)

        dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.progress_layout2)
        dialog.setCancelable(false)

        register_image_btn.setOnClickListener{
            ImagePicker.with(this)
                .crop(1f, 1f).compress(1024)
                .maxResultSize(640, 640).start()
        }

        register_btn.setOnClickListener{
            val email:String = register_email.text.toString()
            val password:String = register_pwd.text.toString()
            val password_correct:String = register_pwd_check.text.toString()
            val nickname:String = register_nickname.text.toString()

            if(email.isEmpty() or password.isEmpty() or password_correct.isEmpty() or nickname.isEmpty()){
                Toast.makeText(this, "전부 입력해주세요", Toast.LENGTH_SHORT).show()
            } else if(password.length < 6) {
                Toast.makeText(this, "비밀번호를 6자리 이상 입력해주세요", Toast.LENGTH_SHORT).show()
            } else if(password != password_correct) {
                Toast.makeText(this, "비밀번호가 다릅니다", Toast.LENGTH_SHORT).show()
            } else {
                dialog.show()
                FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(object : OnCompleteListener<AuthResult>{
                        override fun onComplete(p0: Task<AuthResult>) {
                            if(p0.isComplete){
                                uploadImageToServer(byteArray, email, password, nickname)
                            } else {
                                Toast.makeText(this@RegisterActivity, "다시 시도해주세요", Toast.LENGTH_SHORT).show()
                            }
                        }
                    })
            }
        }

        register_back_btn.setOnClickListener{
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
                Toast.makeText(this, "오류가 발생했습니다! 다시 시도해주세요", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
        }
    }

    private fun addAccount(email: String, password: String, nickname: String, imageUrl:String){
        val accountData = AccountData(email = email, password = password, nickname = nickname, imageUrl = imageUrl, info = "")
        FirebaseDatabase.getInstance().getReference("accounts").push().setValue(accountData)
            .addOnCompleteListener {
                Toast.makeText(this@RegisterActivity, "회원가입이 완료되었습니다", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
                finish()
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            try {
                image = data?.data
                // Uri를 활용하여 ImageView에 가져온 이미지 표시
                val bitmap = ImageDecoder.decodeBitmap(ImageDecoder.createSource(contentResolver, image!!))
                register_image.setImageBitmap(bitmap)
                val byteArrayOutputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
                byteArray = byteArrayOutputStream.toByteArray()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        } else {
            image = null
            byteArray = null
        }
    }
}