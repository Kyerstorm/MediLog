package com.healthcalendar.app.util

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.healthcalendar.app.data.database.entities.Appointment
import com.healthcalendar.app.data.database.entities.AppointmentCategory
import com.healthcalendar.app.data.database.entities.Medication
import com.healthcalendar.app.data.database.entities.MedicationForm
import com.healthcalendar.app.data.database.entities.MedicationLog
import com.healthcalendar.app.data.database.entities.MedicationStatus
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.json.JSONArray
import org.json.JSONObject

object DataImporter {
    
    private val gson = Gson()
    
    data class ImportResult(
        val appointments: List<Appointment> = emptyList(),
        val medications: List<Medication> = emptyList(),
        val logs: List<MedicationLog> = emptyList(),
        val errors: List<String> = emptyList()
    )
    
    // Import from JSON
    fun importFromJson(jsonString: String): ImportResult {
        return try {
            val jsonObject = JSONObject(jsonString)
            val appointments = mutableListOf<Appointment>()
            val medications = mutableListOf<Medication>()
            val logs = mutableListOf<MedicationLog>()
            val errors = mutableListOf<String>()
            
            // Import appointments
            if (jsonObject.has("appointments")) {
                val appointmentsArray = jsonObject.getJSONArray("appointments")
                for (i in 0 until appointmentsArray.length()) {
                    try {
                        val item = appointmentsArray.getJSONObject(i)
                        appointments.add(parseAppointment(item))
                    } catch (e: Exception) {
                        errors.add("Error importing appointment at index $i: ${e.message}")
                    }
                }
            }
            
            // Import medications
            if (jsonObject.has("medications")) {
                val medicationsArray = jsonObject.getJSONArray("medications")
                for (i in 0 until medicationsArray.length()) {
                    try {
                        val item = medicationsArray.getJSONObject(i)
                        medications.add(parseMedication(item))
                    } catch (e: Exception) {
                        errors.add("Error importing medication at index $i: ${e.message}")
                    }
                }
            }
            
            // Import medication logs
            if (jsonObject.has("medicationLogs")) {
                val logsArray = jsonObject.getJSONArray("medicationLogs")
                for (i in 0 until logsArray.length()) {
                    try {
                        val item = logsArray.getJSONObject(i)
                        logs.add(parseMedicationLog(item))
                    } catch (e: Exception) {
                        errors.add("Error importing log at index $i: ${e.message}")
                    }
                }
            }
            
            ImportResult(appointments, medications, logs, errors)
        } catch (e: Exception) {
            ImportResult(errors = listOf("Failed to parse JSON: ${e.message}"))
        }
    }
    
    // Import from CSV
    fun importFromCsv(csvString: String, type: String): ImportResult {
        return try {
            val lines = csvString.lines().filter { it.isNotBlank() }
            if (lines.isEmpty()) {
                return ImportResult(errors = listOf("Empty CSV file"))
            }
            
            val headers = parseCsvLine(lines[0])
            val data = lines.drop(1)
            
            when (type.uppercase()) {
                "APPOINTMENTS" -> {
                    val appointments = data.mapNotNull { line ->
                        try {
                            parseAppointmentFromCsv(parseCsvLine(line), headers)
                        } catch (e: Exception) {
                            null
                        }
                    }
                    ImportResult(appointments = appointments)
                }
                "MEDICATIONS" -> {
                    val medications = data.mapNotNull { line ->
                        try {
                            parseMedicationFromCsv(parseCsvLine(line), headers)
                        } catch (e: Exception) {
                            null
                        }
                    }
                    ImportResult(medications = medications)
                }
                else -> ImportResult(errors = listOf("Unknown CSV type: $type"))
            }
        } catch (e: Exception) {
            ImportResult(errors = listOf("Failed to parse CSV: ${e.message}"))
        }
    }
    
    private fun parseAppointment(json: JSONObject): Appointment {
        return Appointment(
            id = 0, // Will be auto-generated
            title = json.getString("title"),
            description = json.optString("description", ""),
            category = try {
                AppointmentCategory.valueOf(json.getString("category"))
            } catch (e: Exception) {
                AppointmentCategory.OTHER
            },
            startTime = LocalDateTime.parse(json.getString("startTime")),
            endTime = LocalDateTime.parse(json.getString("endTime")),
            location = json.optString("location", ""),
            color = json.optString("color", "#FF6B9D"),
            isRecurring = json.optBoolean("isRecurring", false),
            createdAt = try {
                LocalDateTime.parse(json.optString("createdAt"))
            } catch (e: Exception) {
                Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            }
        )
    }
    
    private fun parseMedication(json: JSONObject): Medication {
        return Medication(
            id = 0, // Will be auto-generated
            name = json.getString("name"),
            dosage = json.getString("dosage"),
            amount = json.optString("amount", "1"), // Default to "1" for backward compatibility
            unit = json.optString("unit", "mg"),
            form = try {
                MedicationForm.valueOf(json.getString("form"))
            } catch (e: Exception) {
                MedicationForm.OTHER
            },
            instructions = json.optString("instructions", ""),
            notes = json.optString("notes", ""),
            photoUri = json.optString("photoUri", null),
            createdAt = try {
                LocalDateTime.parse(json.getString("createdAt"))
            } catch (e: Exception) {
                Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            },
            isActive = json.optBoolean("isActive", true)
        )
    }
    
    private fun parseMedicationLog(json: JSONObject): MedicationLog {
        return MedicationLog(
            id = 0, // Will be auto-generated
            medicationId = json.getLong("medicationId"),
            scheduledTime = LocalDateTime.parse(json.getString("scheduledTime")),
            takenTime = json.optString("takenTime", null)?.let { 
                if (it.isNotEmpty()) LocalDateTime.parse(it) else null 
            },
            status = try {
                MedicationStatus.valueOf(json.getString("status"))
            } catch (e: Exception) {
                MedicationStatus.PENDING
            },
            notes = json.optString("notes", "")
        )
    }
    
    private fun parseAppointmentFromCsv(values: List<String>, headers: List<String>): Appointment {
        val map = headers.zip(values).toMap()
        return Appointment(
            id = 0,
            title = map["Title"] ?: map["title"] ?: "",
            description = map["Description"] ?: map["description"] ?: "",
            category = try {
                AppointmentCategory.valueOf(map["Category"] ?: map["category"] ?: "OTHER")
            } catch (e: Exception) {
                AppointmentCategory.OTHER
            },
            startTime = LocalDateTime.parse(map["Start Time"] ?: map["startTime"] ?: ""),
            endTime = LocalDateTime.parse(map["End Time"] ?: map["endTime"] ?: ""),
            location = map["Location"] ?: map["location"] ?: "",
            color = map["Color"] ?: map["color"] ?: "#FF6B9D",
            isRecurring = false,
            createdAt = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        )
    }
    
    private fun parseMedicationFromCsv(values: List<String>, headers: List<String>): Medication {
        val map = headers.zip(values).toMap()
        return Medication(
            id = 0,
            name = map["Name"] ?: map["name"] ?: "",
            dosage = map["Dosage"] ?: map["dosage"] ?: "",
            amount = map["Amount"] ?: map["amount"] ?: "1", // Default to "1" for backward compatibility
            unit = map["Unit"] ?: map["unit"] ?: "mg",
            form = try {
                MedicationForm.valueOf(map["Form"] ?: map["form"] ?: "OTHER")
            } catch (e: Exception) {
                MedicationForm.OTHER
            },
            instructions = map["Instructions"] ?: map["instructions"] ?: "",
            notes = map["Notes"] ?: map["notes"] ?: "",
            photoUri = null,
            createdAt = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
            isActive = (map["Is Active"] ?: map["isActive"] ?: "true").toBoolean()
        )
    }
    
    private fun parseCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        var current = StringBuilder()
        var inQuotes = false
        
        for (char in line) {
            when {
                char == '"' -> inQuotes = !inQuotes
                char == ',' && !inQuotes -> {
                    result.add(current.toString().trim())
                    current = StringBuilder()
                }
                else -> current.append(char)
            }
        }
        result.add(current.toString().trim())
        
        return result.map { it.removePrefix("\"").removeSuffix("\"") }
    }
}
