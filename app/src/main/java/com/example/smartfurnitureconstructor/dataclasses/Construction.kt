package com.example.smartfurnitureconstructor.dataclasses

class Construction(
    inputId: Int,
    inputName: String,
    inputImage: String,
    inputDescription: String,
    inputRating: Int,
    inputType: String
): CatalogEntity(inputId, inputName, inputImage, inputDescription,
    inputRating)  {
    var type: String = inputType
}