package com.example.books.ui.dashboard.profile.edit

import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.books.MainActivity
import com.example.books.R
import com.example.books.databinding.FragmentProfileEditBinding
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class ProfileEditFragment : Fragment(R.layout.fragment_profile_edit) {

    //binding
    private lateinit var binding: FragmentProfileEditBinding
    //firebase auth
    private lateinit var firebaseAuth: FirebaseAuth

    //Image uri (which we will pick)
    private var imageUri: Uri? = null

    //progress dialog
    private lateinit var progressDialog: ProgressDialog

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentProfileEditBinding.bind(view)

        //setup progress dialog
        progressDialog = ProgressDialog(requireContext())
        progressDialog.setTitle("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)

        //init firebase auth
        firebaseAuth = FirebaseAuth.getInstance()
        loadUserInfo()

        binding.profileImageV.setOnClickListener {
            showImageAttachMenu()
        }

        binding.updateBtn.setOnClickListener {
            validateData()
        }

        //back button
        binding.btnBack.setOnClickListener {
            (activity as MainActivity?)?.onBackPressed()
        }
    }

    private var name = ""
    private fun validateData() {
        //get data
        name = binding.username.text.toString().trim()

        //validate data
        if (name.isEmpty()){
            //name not entered
            Toast.makeText(requireContext(), "Enter name", Toast.LENGTH_LONG).show()
        } else {
            //name is entered
            if (imageUri == null){
                //need to update without image
                updateProfile("")
            } else {
                //need to update with image
                uploadImage()
            }
        }
    }

    private fun uploadImage() {
        progressDialog.setMessage("Upload profile image")
        progressDialog.show()

        //image path and name, use uid to replace previous
        val filePathAndName = "ProfileImage/"+firebaseAuth.uid

        //storage reference
        val reference = FirebaseStorage.getInstance().getReference(filePathAndName)
        reference.putFile(imageUri!!)
            .addOnSuccessListener { taskSnapshot ->
                //image uploaded, get url of uploaded image
                val uriTask: Task<Uri> = taskSnapshot.storage.downloadUrl
                while (!uriTask.isSuccessful);
                val uploadedImageUrl = "${uriTask.result}"

                updateProfile(uploadedImageUrl)

            }
            .addOnFailureListener{ e ->
                //failed to upload image
                progressDialog.dismiss()
                Toast.makeText(requireContext(), "Failed to upload image due to ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun updateProfile(uploadedImageUrl: String) {
        progressDialog.setMessage("Updating profile...")

        //setup info to update to db
        val hashMap = HashMap<String, Any>()
        hashMap["name"] = "$name"
        hashMap["profileImage"] = uploadedImageUrl

        //update to db
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!)
            .updateChildren(hashMap)
            .addOnSuccessListener {
                //profile updated
                progressDialog.dismiss()
                Toast.makeText(requireContext(), "profile updated", Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener { e ->
                //failed to upload image
                progressDialog.dismiss()
                Toast.makeText(requireContext(), "Failed to update profile due to ${e.message}", Toast.LENGTH_LONG).show()

            }
    }

    private fun loadUserInfo() {
        //db reference to load user info
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    //get data
                    val name = snapshot.child("name").value.toString()
                    val profileImage = snapshot.child("profileImage").value.toString()

                    //set data
                    binding.username.setText(name)
                    //set image
//                    try {
                        Glide.with(requireContext())
                            .load(profileImage)
                            .placeholder(R.drawable.ic_person_gray)
                            .into(binding.profileImageV)
//                    } catch (e: Exception){
//                        Toast.makeText(requireContext(), "catch: error", Toast.LENGTH_LONG).show()
//                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
    }

    private fun showImageAttachMenu(){
        //show popup menu with options Camera, Gallery to pick image

        //setup popup menu
        val popupMenu = PopupMenu(requireContext(),binding.profileImageV)
        popupMenu.menu.add(Menu.NONE, 0, 0, "Camera")
        popupMenu.menu.add(Menu.NONE, 1, 1, "Gallery")
        popupMenu.show()

        //handle popup menu item click
        popupMenu.setOnMenuItemClickListener { item ->
            //get id of clicked item
            val id = item.itemId
            if (id == 0){
                //camera clicked
                pickImageCamera()
            } else if (id == 1){
                //Gallery clicked                                                                                                                                                                                                                                                                                            clicked
                pickImageGallery()
            }
            true
        }
    }

    private fun pickImageCamera() {
        //intent to pick image from Camera
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "Temp_Title")
        values.put(MediaStore.Images.Media.DESCRIPTION, "Temp_Description")

        imageUri = requireActivity().contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        cameraActivityResultLauncher.launch(intent)
    }

    private fun pickImageGallery() {
        //intent to pick image from gallery
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        galleryActivityResultLauncher.launch(intent)
    }

    //used to handle result of camera intent (new way in replacement of startActivityForResults)
    private val cameraActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        ActivityResultCallback { result ->
            //get uri of image
            if (result.resultCode == Activity.RESULT_OK){
                val data = result.data
//                imageUri = data!!.data  no need we already have image in imageUri in camera case

                //set to imageView
                binding.profileImageV.setImageURI(imageUri)
            } else {
                //cancelled
                Toast.makeText(requireContext(), "Cancelled", Toast.LENGTH_LONG).show()
            }
        }
    )

    //used to handle result of gallery intent (new way in replacement of startActivityForResults)
    private val galleryActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        ActivityResultCallback { result ->
            //get uri of image
            if (result.resultCode == Activity.RESULT_OK){
                val data = result.data
                imageUri = data!!.data

                //set to imageView
                binding.profileImageV.setImageURI(imageUri)
            } else {
                //cancelled
                Toast.makeText(requireContext(), "Cancelled", Toast.LENGTH_LONG).show()
            }
        }
    )

}