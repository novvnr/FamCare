package com.app.famcare.model

import android.os.Parcel
import android.os.Parcelable

data class Nanny(
    var id: String = "",
    val age: String = "",
    val experience: String = "",
    val gender: String = "",
    val location: String = "",
    val name: String = "",
    val rate: String = "",
    val pricing: Int = 0,
    val skills: List<String> = listOf(),
    val type: String = "",
    val pict: String = "",
    val contact: String = "",
    val imageUrl: String = "",
    var isBookmarked: Boolean = false
) : Parcelable {
    constructor(parcel: Parcel) : this(
        id = parcel.readString() ?: "",
        age = parcel.readString() ?: "",
        experience = parcel.readString() ?: "",
        gender = parcel.readString() ?: "",
        location = parcel.readString() ?: "",
        name = parcel.readString() ?: "",
        rate = parcel.readString() ?: "",
        pricing = parcel.readInt(),
        skills = parcel.createStringArrayList() ?: listOf(),
        type = parcel.readString() ?: "",
        pict = parcel.readString() ?: "",
        contact = parcel.readString() ?: "",
        imageUrl = parcel.readString() ?: "",
        isBookmarked = parcel.readByte() != 0.toByte()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(age)
        parcel.writeString(experience)
        parcel.writeString(gender)
        parcel.writeString(location)
        parcel.writeString(name)
        parcel.writeString(rate)
        parcel.writeInt(pricing)
        parcel.writeStringList(skills)
        parcel.writeString(type)
        parcel.writeString(pict)
        parcel.writeString(contact)
        parcel.writeString(imageUrl)
        parcel.writeByte(if (isBookmarked) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Nanny> {
        override fun createFromParcel(parcel: Parcel): Nanny {
            return Nanny(parcel)
        }

        override fun newArray(size: Int): Array<Nanny?> {
            return arrayOfNulls(size)
        }
    }
}