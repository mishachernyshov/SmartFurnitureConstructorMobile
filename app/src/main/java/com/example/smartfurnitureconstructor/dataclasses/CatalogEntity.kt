package com.example.smartfurnitureconstructor.dataclasses

open class CatalogEntity(inputId: Int,
                         inputName: String,
                         inputImage: String,
                         inputDescription: String,
                         inputRating: Int
): CatalogFitable
{
    override var id: Int = inputId
    override var name: String = inputName
    override var image: String = inputImage
    override var description: String = inputDescription
    override var rating: Int = inputRating
}
