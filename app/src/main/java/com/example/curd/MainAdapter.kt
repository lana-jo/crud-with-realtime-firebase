package com.example.curd

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.amulyakhare.textdrawable.util.ColorGenerator
import com.bumptech.glide.Glide


class MainAdapter(
//menampilkan data pada ModelBarang dengan recycleView
    private val context: Context,
    val daftarBarang: ArrayList<ModelBarang?>
) : RecyclerView.Adapter<MainViewHolder>() {
    private val listener: FirebasedataListener
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_barang, parent, false)
        return MainViewHolder(view)
    }

    //menampilkan data
    override fun onBindViewHolder(holder: MainViewHolder, position: Int) {

        val generator = ColorGenerator.MATERIAL
        val color = generator.randomColor
        holder.view.setCardBackgroundColor(color)
//    diambil dari ModelBarang.kt
        holder.namaBarang.text = "Nama: " + daftarBarang.get(position)?.nama
        holder.merkBarang.text = "Merk: " + daftarBarang.get(position)?.merk
        holder.hargaBarang.text = "Harga: " + daftarBarang.get(position)?.harga
        Glide.with(context).load(daftarBarang.get(position)?.filepath)
        holder.view.setOnClickListener {
            listener.onDataClick(daftarBarang.get(position), position)
        }
    }

    override fun getItemCount(): Int {
        return daftarBarang.size
    }

    //tampilkan data dari ModelBarang dan child barang yang terdapat pada firebase realtime
    interface FirebasedataListener {

        fun onDataClick(barang: ModelBarang?, position: Int)
    }

    init {
        //membaca apa yang dilaksanakan FirebasedataLisener
        listener = context as FirebasedataListener
    }
}

