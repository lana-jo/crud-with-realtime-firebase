package com.example.curd

import android.graphics.Bitmap
import com.google.firebase.storage.StorageReference
import android.net.Uri

class ModelBarang()
{

//inisasi variabel.

    var key: String? = null
    var harga: String? = null
    var merk: String? = null
    var nama: String? = null
    var filepath: String? = null
//inisialisi objek sebelum dipakai
    constructor(namaBarang: String?, merkBarang: String?, hargaBarang: String?, foto: String?) : this() {
        nama = namaBarang
        merk = merkBarang
        harga= hargaBarang
        filepath= foto
    }
//getter setter untuk melindungi dalam penggunaan data
    override fun toString(): String {
        return super.toString()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ModelBarang) return false

        if (key != other.key) return false
        if (filepath != other.filepath) return false

        return true
    }

    override fun hashCode(): Int {
        var result = key?.hashCode() ?: 0
        result = 31 * result + (filepath?.hashCode() ?: 0)
        return result
    }
}