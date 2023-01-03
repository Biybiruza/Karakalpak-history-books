package com.example.books.ui.dashboard.profile

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.books.R
import com.example.books.data.ModelComment
import com.example.books.databinding.RowCommentBinding
import com.example.books.ui.dashboard.admin.MyApplication
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AdapterComment(val context: Context, val firebaseAuth: FirebaseAuth) : RecyclerView.Adapter<AdapterComment.CommentViewHolder>(){

    inner class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val binding = RowCommentBinding.bind(itemView)
        fun populateModel(modelComment: ModelComment){
            //set data
            binding.dateTv.text = MyApplication.formatTimeStamp(modelComment.timestamp.toLong())
            binding.commentTv.text = modelComment.comment

            //we don't have user name, profile picture but we have user uid, so we will load using that uid
            loadUserDetails(binding, modelComment)

            itemView.setOnClickListener {
                /*Requirement to delete a comment
                * 1) User must be logged in
                * 2) uid in comment (to be deleted) must be same as uid of current user i.e. user can delete only this own comment*/
                if (firebaseAuth.currentUser != null && firebaseAuth.uid == modelComment.uid){
                    deleteCommentDialog(modelComment, binding)
                }
            }
        }
    }

    private fun deleteCommentDialog(modelComment: ModelComment, binding: RowCommentBinding) {
        //alert dialog
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Delete comment")
            .setMessage("Are you sure you want to delete this comment")
            .setPositiveButton("Delete"){ d, e ->
                val bookId = modelComment.bookId
                val commentId = modelComment.id

                val ref = FirebaseDatabase.getInstance().getReference("Books")
                ref.child(bookId).child("Comments").child(commentId)
                    .removeValue()
                    .addOnSuccessListener {
                        Toast.makeText(context, "Comment deleted", Toast.LENGTH_LONG).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(context, "Failed to deleted comment due to ${e.message}", Toast.LENGTH_LONG).show()
                    }
            }
            .setNegativeButton("Cancel"){ d, e ->
                d.dismiss()
            }
            .show()
    }

    private fun loadUserDetails(binding: RowCommentBinding, modelComment: ModelComment) {
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(modelComment.uid)
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    //get name, profile image
                    val name = snapshot.child("name").value.toString()
                    val profileImage = snapshot.child("profileImage").value.toString()

                    //set data
                    binding.nameTv.text = name
                    try {
                        Glide.with(context)
                            .load(profileImage)
                            .placeholder(R.drawable.ic_person_gray)
                            .into(binding.profileIv)
                    } catch (e: Exception){
                        //in case of exception due to image is empty or null or other reason, set default image
                        binding.profileIv.setImageResource(R.drawable.ic_person_gray)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
    }

    //comment list
    var commentList = arrayListOf<ModelComment>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val item = LayoutInflater.from(parent.context).inflate(R.layout.row_comment, parent, false)
        return CommentViewHolder(item)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        holder.populateModel(commentList[position])
    }

    override fun getItemCount(): Int = commentList.size
}