package com.app.famcare.repository

import com.app.famcare.model.Nanny
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class NannyRepository {

    private val db = FirebaseFirestore.getInstance()

    fun getNannies(
        onSuccess: (List<Nanny>) -> Unit, onFailure: (Exception) -> Unit
    ) {
        db.collection("Nanny").get().addOnSuccessListener { querySnapshot ->
            val nannyList = mutableListOf<Nanny>()
            for (document in querySnapshot) {
                val nanny = document.toObject(Nanny::class.java).apply {
                    id = document.id
                }
                nannyList.add(nanny)
            }
            onSuccess(nannyList)
        }.addOnFailureListener { exception ->
            onFailure(exception)
        }
    }

    fun getNannies(
        filterCriteria: Map<String, Any>,
        onSuccess: (List<Nanny>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        var query: Query = db.collection("Nanny")

        for ((key, value) in filterCriteria) {
            when (key) {
                "gender" -> query = query.whereEqualTo("gender", value)
                "type" -> query = query.whereEqualTo("type", value)
                "location" -> query = query.whereIn("location", value as List<String>)
                "skills" -> query = query.whereArrayContains("skills", value)
                "experience" -> query = query.whereEqualTo("experience", value)
            }
        }

        query.get().addOnSuccessListener { querySnapshot ->
            val nannyList = mutableListOf<Nanny>()
            for (document in querySnapshot) {
                val nanny = document.toObject(Nanny::class.java).apply {
                    id = document.id
                }
                nannyList.add(nanny)
            }
            onSuccess(nannyList)
        }.addOnFailureListener { exception ->
            onFailure(exception)
        }
    }

    fun searchNannies(
        queryText: String, onSuccess: (List<Nanny>) -> Unit, onFailure: (Exception) -> Unit
    ) {
        val lowercaseQueryText = queryText.lowercase()
        db.collection("Nanny").get().addOnSuccessListener { querySnapshot ->
            val result = mutableListOf<Nanny>()
            for (document in querySnapshot) {
                val nanny = document.toObject(Nanny::class.java).apply {
                    id = document.id
                }

                if (nanny.age.lowercase().contains(lowercaseQueryText) ||
                    nanny.experience.lowercase().contains(lowercaseQueryText) ||
                    nanny.gender.lowercase().contains(lowercaseQueryText) ||
                    nanny.location.lowercase().contains(lowercaseQueryText) ||
                    nanny.name.lowercase().contains(lowercaseQueryText) ||
                    nanny.type.lowercase().contains(lowercaseQueryText) ||
                    nanny.skills.any { it.lowercase().contains(lowercaseQueryText) }
                ) {
                    result.add(nanny)
                }
                else {
                    val pricingQuery = queryText.toIntOrNull()
                    if (pricingQuery != null && nanny.pricing == pricingQuery) {
                        result.add(nanny)
                    }
                }
            }
            onSuccess(result)
        }.addOnFailureListener { exception ->
            onFailure(exception)
        }
    }
}