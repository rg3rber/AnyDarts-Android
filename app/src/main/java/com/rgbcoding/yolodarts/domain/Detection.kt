package com.rgbcoding.yolodarts.domain

/* TODO: decide if you want to run inference on device.
 * Pro:
 * - No weird problems with connection and setup
 * - No need to transmit image
 * - Can use model results directly
 *
 * Con:
 * - Have to either embed python processing pipeline in app
 * - or translate to kotlin
 * - seems like a lot of work and potential sticking points
 *
 *  Will hold YOLO output
 */
data class Detection(
    val bboxes: List<Float>? = null
)
