package com.chrissytopher.source

abstract class NotificationSender {
    abstract fun sendNotification(title: String, body: String)
}