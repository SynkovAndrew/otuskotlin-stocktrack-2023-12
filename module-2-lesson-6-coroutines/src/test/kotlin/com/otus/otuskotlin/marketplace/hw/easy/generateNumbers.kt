package com.otus.otuskotlin.marketplace.hw.easy

fun generateNumbers(): List<Int> {
    return (0..10000).map { (0..100).random() }
}