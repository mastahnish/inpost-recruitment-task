package pl.inpost.shipment.implementation.data.remote.model

import java.time.ZonedDateTime

data class EventLogNetwork(
    val name: String,
    val date: ZonedDateTime
)