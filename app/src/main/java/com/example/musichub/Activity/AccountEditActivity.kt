package com.example.musichub.Activity

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.ImageDecoder
import android.graphics.drawable.ColorDrawable
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
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.musichub.Data.AccountData
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
import de.hdodenhof.circleimageview.CircleImageView
import java.io.ByteArrayOutputStream

class AccountEditActivity : AppCompatActivity() {

    lateinit var account_edit_back_btn: ImageView
    lateinit var account_edit_image: CircleImageView
    lateinit var account_edit_image_btn: Button
    lateinit var account_nickname_edit: EditText
    lateinit var account_info_edit: EditText
    lateinit var account_info_length: TextView
    lateinit var account_edit_save_btn: Button
    lateinit var password_edit_btn: Button
    lateinit var dialog: Dialog

    var key: String = ""
    private var image: Uri? = null
    private var byteArray: ByteArray? = null
    private val myEmail: String = FirebaseAuth.getInstance().currentUser?.email.toString()

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_edit)

        dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.progress_layout2)
        dialog.setCancelable(false)

        account_edit_back_btn = findViewById(R.id.account_edit_back_btn)
        account_edit_image = findViewById(R.id.account_edit_image)
        account_edit_image_btn = findViewById(R.id.account_edit_image_btn)
        account_info_edit = findViewById(R.id.account_info_edit)
        account_info_length = findViewById(R.id.account_info_length)
        account_edit_save_btn = findViewById(R.id.account_edit_save_btn)
        account_nickname_edit = findViewById(R.id.account_nickname_edit)
        password_edit_btn = findViewById(R.id.password_edit_btn)

        getAccountData()

        account_edit_back_btn.setOnClickListener{
            finish()
        }

        account_edit_image_btn.setOnClickListener{
            ImagePicker.with(this@AccountEditActivity)
                .crop(1f, 1f).compress(1024)
                .maxResultSize(640, 640)
                .createIntent { intent -> imageLauncher.launch(intent) }
        }

        // 곡 설명 터치시 editText가 스크롤 되도록 설정
        account_info_edit.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                if (account_info_edit.hasFocus()) {
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

        account_info_edit.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            @SuppressLint("SetTextI18n")
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val length: Int = account_info_edit.text.length
                account_info_length.text = "$length / 2000"
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // 저장
        account_edit_save_btn.setOnClickListener{
            if(account_nickname_edit.text.toString().isEmpty()){
                Toast.makeText(this@AccountEditActivity, "닉네임을 입력해주세요", Toast.LENGTH_SHORT).show()
            } else if(byteArray == null){
                dialog.show()
                val hashMap = HashMap<String, Any>()
                hashMap["nickname"] = account_nickname_edit.text.toString()
                hashMap["info"] = account_info_edit.text.toString()

                FirebaseDatabase.getInstance().getReference("accounts").child(key).updateChildren(hashMap)
                editNickName(myEmail, account_nickname_edit.text.toString())
            } else {
                dialog.show()
                uploadImageToServer(byteArray!!, myEmail)
            }
        }

        password_edit_btn.setOnClickListener{
            val alertEx: AlertDialog.Builder = AlertDialog.Builder(this@AccountEditActivity)
            alertEx.setMessage("비밀번호를 변경하시겠습니까?\n비밀번호를 재설정하는 메일을 보냅니다")
            alertEx.setNegativeButton("네") { _, _ ->
                Toast.makeText(this@AccountEditActivity, "해당 이메일로 비밀번호를 재설정하는 메일을 보냈습니다", Toast.LENGTH_SHORT).show()
                FirebaseAuth.getInstance().sendPasswordResetEmail(myEmail)
            }
            alertEx.setPositiveButton("아니요") { dialog, _ ->
                dialog.dismiss()
            }
            val alert = alertEx.create()
            alert.window!!.setBackgroundDrawable(ColorDrawable(Color.DKGRAY))
            alert.show()
        }
    }

    private fun getAccountData(){
        FirebaseDatabase.getInstance().getReference("accounts").orderByChild("email").equalTo(myEmail).limitToFirst(1)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                @SuppressLint("SetTextI18n")
                override fun onDataChange(snapshot: DataSnapshot) {
                    for(ds: DataSnapshot in snapshot.children) {
                        val data = ds.getValue<AccountData>()
                        if(data != null){
                            key = ds.key.toString()
                            account_nickname_edit.setText(data.nickname)
                            account_info_edit.setText(data.info)
                            account_info_length.text = data.info.length.toString() + " / 2000"

                            if(data.imageUrl == ""){
                                account_edit_image.setImageResource(R.drawable.baseline_account_circle_24)
                            } else {
                                Glide.with(this@AccountEditActivity).load(data.imageUrl).into(account_edit_image)
                            }
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun uploadImageToServer(byteArray: ByteArray, email: String){
        FirebaseStorage.getInstance().getReference().child("Accounts_Thumbnails")
            .child(email).putBytes(byteArray).addOnSuccessListener { p0 ->
                val task: Task<Uri> = p0!!.storage.downloadUrl
                while (!task.isComplete);
                val urlSong: String = task.result.toString()

                val hashMap = HashMap<String, Any>()
                hashMap["nickname"] = account_nickname_edit.text.toString()
                hashMap["info"] = account_info_edit.text.toString()
                hashMap["imageUrl"] = urlSong

                FirebaseDatabase.getInstance().getReference("accounts").child(key).updateChildren(hashMap)
                editNickName(myEmail, account_nickname_edit.text.toString())
            }.addOnFailureListener {
                Toast.makeText(this, "오류가 발생했습니다! 다시 시도해주세요", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
    }

    private fun editNickName(email: String, nickname: String){
        FirebaseDatabase.getInstance().getReference("Songs").orderByChild("email").equalTo(email)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for(ds: DataSnapshot in snapshot.children) {
                        val getKey = ds.key.toString()

                        val hashMap = HashMap<String, Any>()
                        hashMap["songArtist"] = nickname
                        FirebaseDatabase.getInstance().getReference("Songs").child(getKey).updateChildren(hashMap)
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })

        FirebaseDatabase.getInstance().getReference("Comments").orderByChild("email").equalTo(email)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for(ds: DataSnapshot in snapshot.children) {
                        val getKey = ds.key.toString()

                        val hashMap = HashMap<String, Any>()
                        hashMap["nickname"] = nickname
                        FirebaseDatabase.getInstance().getReference("Comments").child(getKey).updateChildren(hashMap)
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })

        FirebaseDatabase.getInstance().getReference("PlayLists").orderByChild("email").equalTo(email)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for(ds: DataSnapshot in snapshot.children) {
                        val getKey = ds.key.toString()

                        val hashMap = HashMap<String, Any>()
                        hashMap["nickname"] = nickname
                        FirebaseDatabase.getInstance().getReference("PlayLists").child(getKey).updateChildren(hashMap)
                    }
                    dialog.dismiss()
                    Toast.makeText(this@AccountEditActivity, "정보가 수정되었습니다", Toast.LENGTH_SHORT).show()
                    finish()
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private val imageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result: ActivityResult ->
        val resultCode = result.resultCode
        val data = result.data

        if (resultCode == Activity.RESULT_OK) {
            image = data?.data
            // Uri를 활용하여 ImageView에 가져온 이미지 표시
            val bitmap = ImageDecoder.decodeBitmap(ImageDecoder.createSource(contentResolver, image!!))
            account_edit_image.setImageBitmap(bitmap)
            val byteArrayOutputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
            byteArray = byteArrayOutputStream.toByteArray()
        } else {
            image = null
            byteArray = null
        }
    }
}