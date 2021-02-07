package com.example.smartfurnitureconstructor.dataclasses

class Component(
    inputId: Int,
    inputName: String,
    inputImage: String,
    inputDescription: String,
    inputRating: Int,
    inputCategory: String,
    inputManufacturer: String
): CatalogEntity(inputId, inputName, inputImage, inputDescription,
    inputRating) {
    var category: String = inputCategory
    var manufacturer: String = inputManufacturer
}