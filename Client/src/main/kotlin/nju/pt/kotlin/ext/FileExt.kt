package nju.pt.kotlin.ext

import java.io.File

fun File.mkdirIfEmpty() = apply {
    if (!this.exists()) {
        this.parentFile.mkdirs()
    }
}