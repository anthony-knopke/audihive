package com.darkaquadigital.audihive.data

data class Artist(
    val id: Int,
    val name: String,
    val image: String // Assuming URL or file path
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Artist) return false
        return name == other.name
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }
}
