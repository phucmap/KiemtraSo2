package com.example.baikiemtraso2

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.SearchView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.baikiemtraso2.databinding.ActivityMainBinding
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {

    private lateinit var productRecyclerView : RecyclerView
    private lateinit var productAdapter: ProductAdapter
    private lateinit var productList : ArrayList<Product>
    private lateinit var mDbRef : DatabaseReference

    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.add.setOnClickListener {
            addProduct()
        }

        binding.imageAvatar.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent,0)
        }


        mDbRef = FirebaseDatabase.getInstance().reference

        productRecyclerView = findViewById(R.id.listProduct)
        productList = ArrayList()
        productAdapter = ProductAdapter(this,productList)

        productRecyclerView.layoutManager =LinearLayoutManager(this)
        productRecyclerView.adapter = productAdapter


        mDbRef.child("product")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    productList.clear()
                    for(i in snapshot.children) {
                        val product = i.getValue(Product::class.java)
                        productList.add(product!!)
                    }
                    productAdapter.notifyDataSetChanged()
                }
                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })


    }

    private var imageUrl : Uri?= null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {
            imageUrl = data.data
            binding.textView.text = ""
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver,imageUrl)
            val bitmapDrawable = BitmapDrawable(bitmap)
            binding.imageAvatar.setBackgroundDrawable(bitmapDrawable)
        }
    }

    private fun search(str : String) {
        var item : ArrayList<Product> = ArrayList()
        for(i in productList) {
            if(i.tenSp.lowercase().contains(str.lowercase())){
                item.add(i)
            }
        }
        if(item.isEmpty()) {
            Toast.makeText(this,"Null",Toast.LENGTH_SHORT).show()
        } else {
            productAdapter.setFilteredList(item)
        }
    }

    private fun addProduct () {
        if(imageUrl==null) return
        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")
        ref.putFile(imageUrl!!)
            .addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener {
                    val ref = FirebaseDatabase.getInstance().getReference("product")
                    val product = Product(ref.push().key!!,binding.itensp.text.toString(),
                        binding.iloaisp.text.toString(),binding.igiasanpham.text.toString(),it.toString())
                    ref.push().setValue(product)
                        .addOnCompleteListener{
                        }
                    reset()
                    Toast.makeText(this,"Thêm thành công",Toast.LENGTH_SHORT).show()
                }
            }
    }
    private fun reset() {
        binding.igiasanpham.setText("")
        binding.iloaisp.setText("")
        binding.itensp.setText("")
    }
}