package com.example.curd

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
//inisiasi apa yang akan ditampilkan
class MainViewHolder (itemView: View): RecyclerView.ViewHolder(itemView) {
    @JvmField
    var namaBarang: TextView
    @JvmField
    var merkBarang:TextView

    @JvmField
    var hargaBarang:TextView

    @JvmField
    var filepath : ImageView

    @JvmField
    var view: CardView
    //penempatan data yang akan ditampilkan
    init {
        namaBarang = itemView.findViewById(R.id.nama_barang)
        merkBarang= itemView.findViewById(R.id.merk_barang)
        hargaBarang= itemView.findViewById(R.id.harga_barang)
        filepath = itemView.findViewById(R.id.image)
        view = itemView.findViewById(R.id.cvMain)
    }
}