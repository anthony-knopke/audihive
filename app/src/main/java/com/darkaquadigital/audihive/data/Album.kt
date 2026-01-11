package com.darkaquadigital.audihive.data

data class Album(
    val id: Int,
    val name: String,
    val artist: String,
    val coverImage: String // Assuming URL or file path
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Album) return false
        return name == other.name && artist == other.artist
    }

    override fun hashCode(): Int {
        return 31 * name.hashCode() + artist.hashCode()
    }
}
