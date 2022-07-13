package nju.pt.kotlin.ext

import java.io.File

fun File.mkdirIfEmpty() = apply {
    if (this.isFile and !this.exists()) {
        this.parentFile.mkdirs()
    }
    if (this.isDirectory and !this.exists()) {
        this.mkdirs()
    }
}