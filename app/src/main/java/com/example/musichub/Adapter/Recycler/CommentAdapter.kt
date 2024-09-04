package com.example.musichub.Adapter.Recycler

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.musichub.Data.CommentData
import com.example.musichub.Interface.CommentListener
import com.example.musichub.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import de.hdodenhof.circleimageview.CircleImageView

class CommentAdapter(val list: MutableList<CommentData>, val keyList: MutableList<String>, val commentListener: CommentListener): RecyclerView.Adapter<CommentAdapter.ViewHolder>() {

    val email: String = FirebaseAuth.getInstance().currentUser?.email.toString()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.comment_layout, parent, false)
        return ViewHolder(view)
    }

    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (list[position].imageUrl == "") {
            holder.commentUserImage.setImageResource(R.drawable.baseline_account_circle_24)
        } else {
            Glide.with(holder.itemView).load(list[position].imageUrl).into(holder.commentUserImage)
        }

        if (list[position].email == email) {
            holder.commentDelete.visibility = View.VISIBLE
        } else {
            holder.commentDelete.visibility = View.GONE
        }

        holder.commentUserName.text = list[position].nickname + " · " + list[position].time.substring(0, 10)
        holder.commentText.text = list[position].comment

        holder.commentUserImage.setOnClickListener{
            commentListener.goProfile(list[position].email)
        }

        holder.commentDelete.setOnClickListener {
            val alert_ex: AlertDialog.Builder = AlertDialog.Builder(holder.itemView.context)
            alert_ex.setMessage(holder.itemView.context.getString(R.string.comment_delete))
            alert_ex.setNegativeButton(holder.itemView.context.getString(R.string.yes)) { _, _ ->
                list.removeAt(position)
                deleteComment(keyList[position])
                notifyDataSetChanged()
            }
            alert_ex.setPositiveButton(holder.itemView.context.getString(R.string.no)) { dialog, _ ->
                dialog.dismiss()
            }
            val alert = alert_ex.create()
            alert.window!!.setBackgroundDrawable(ColorDrawable(Color.DKGRAY))
            alert.show()
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val commentUserImage: CircleImageView = itemView.findViewById(R.id.comment_user_image)
        val commentUserName: TextView = itemView.findViewById(R.id.comment_user_name)
        val commentText: TextView = itemView.findViewById(R.id.comment_text)
        val commentDelete: ImageView = itemView.findViewById(R.id.comment_delete)
    }

    private fun deleteComment(key: String){
        FirebaseDatabase.getInstance().getReference("Comments").child(key).removeValue()
    }
}