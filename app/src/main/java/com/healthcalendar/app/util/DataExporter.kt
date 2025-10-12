package com.healthcalendar.app.util

import android.util.Xml
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.healthcalendar.app.data.database.entities.Appointment
import com.healthcalendar.app.data.database.entities.Medication
import com.healthcalendar.app.data.database.entities.MedicationLog
import org.xmlpull.v1.XmlSerializer
import java.io.StringWriter

object DataExporter {
    
    private val gson: Gson = GsonBuilder()
        .setPrettyPrinting()
        .create()
    
    // CSV Export
    fun exportToCsv(
        appointments: List<Appointment>,
        medications: List<Medication>,
        logs: List<MedicationLog>
    ): String {
        return CsvExporter.exportAll(appointments, medications, logs)
    }
    
    fun exportAppointmentsToCsv(appointments: List<Appointment>): String {
        return CsvExporter.exportAppointments(appointments)
    }
    
    fun exportMedicationsToCsv(medications: List<Medication>): String {
        return CsvExporter.exportMedications(medications)
    }
    
    // JSON Export
    fun exportToJson(
        appointments: List<Appointment>,
        medications: List<Medication>,
        logs: List<MedicationLog>
    ): String {
        val data = mapOf(
            "appointments" to appointments,
            "medications" to medications,
            "medicationLogs" to logs,
            "exportDate" to System.currentTimeMillis(),
            "version" to "1.0"
        )
        return gson.toJson(data)
    }
    
    fun exportAppointmentsToJson(appointments: List<Appointment>): String {
        val data = mapOf(
            "appointments" to appointments,
            "exportDate" to System.currentTimeMillis()
        )
        return gson.toJson(data)
    }
    
    fun exportMedicationsToJson(medications: List<Medication>): String {
        val data = mapOf(
            "medications" to medications,
            "exportDate" to System.currentTimeMillis()
        )
        return gson.toJson(data)
    }
    
    // XML Export
    fun exportToXml(
        appointments: List<Appointment>,
        medications: List<Medication>,
        logs: List<MedicationLog>
    ): String {
        val serializer: XmlSerializer = Xml.newSerializer()
        val writer = StringWriter()
        
        serializer.setOutput(writer)
        serializer.startDocument("UTF-8", true)
        serializer.startTag("", "healthData")
        serializer.attribute("", "version", "1.0")
        serializer.attribute("", "exportDate", System.currentTimeMillis().toString())
        
        // Appointments
        serializer.startTag("", "appointments")
        appointments.forEach { appointment ->
            serializer.startTag("", "appointment")
            serializer.attribute("", "id", appointment.id.toString())
            
            serializer.startTag("", "title")
            serializer.text(appointment.title)
            serializer.endTag("", "title")
            
            serializer.startTag("", "description")
            serializer.text(appointment.description)
            serializer.endTag("", "description")
            
            serializer.startTag("", "category")
            serializer.text(appointment.category.name)
            serializer.endTag("", "category")
            
            serializer.startTag("", "startTime")
            serializer.text(appointment.startTime.toString())
            serializer.endTag("", "startTime")
            
            serializer.startTag("", "endTime")
            serializer.text(appointment.endTime.toString())
            serializer.endTag("", "endTime")
            
            serializer.startTag("", "location")
            serializer.text(appointment.location)
            serializer.endTag("", "location")
            
            serializer.startTag("", "color")
            serializer.text(appointment.color)
            serializer.endTag("", "color")
            
            serializer.endTag("", "appointment")
        }
        serializer.endTag("", "appointments")
        
        // Medications
        serializer.startTag("", "medications")
        medications.forEach { medication ->
            serializer.startTag("", "medication")
            serializer.attribute("", "id", medication.id.toString())
            
            serializer.startTag("", "name")
            serializer.text(medication.name)
            serializer.endTag("", "name")
            
            serializer.startTag("", "dosage")
            serializer.text(medication.dosage)
            serializer.endTag("", "dosage")
            
            serializer.startTag("", "amount")
            serializer.text(medication.amount)
            serializer.endTag("", "amount")
            
            serializer.startTag("", "unit")
            serializer.text(medication.unit)
            serializer.endTag("", "unit")
            
            serializer.startTag("", "form")
            serializer.text(medication.form.name)
            serializer.endTag("", "form")
            
            serializer.startTag("", "instructions")
            serializer.text(medication.instructions)
            serializer.endTag("", "instructions")
            
            serializer.startTag("", "notes")
            serializer.text(medication.notes)
            serializer.endTag("", "notes")
            
            serializer.startTag("", "isActive")
            serializer.text(medication.isActive.toString())
            serializer.endTag("", "isActive")
            
            serializer.endTag("", "medication")
        }
        serializer.endTag("", "medications")
        
        // Medication Logs
        serializer.startTag("", "medicationLogs")
        logs.forEach { log ->
            serializer.startTag("", "log")
            serializer.attribute("", "id", log.id.toString())
            serializer.attribute("", "medicationId", log.medicationId.toString())
            
            serializer.startTag("", "scheduledTime")
            serializer.text(log.scheduledTime.toString())
            serializer.endTag("", "scheduledTime")
            
            serializer.startTag("", "takenTime")
            serializer.text(log.takenTime?.toString() ?: "")
            serializer.endTag("", "takenTime")
            
            serializer.startTag("", "status")
            serializer.text(log.status.name)
            serializer.endTag("", "status")
            
            serializer.startTag("", "notes")
            serializer.text(log.notes)
            serializer.endTag("", "notes")
            
            serializer.endTag("", "log")
        }
        serializer.endTag("", "medicationLogs")
        
        serializer.endTag("", "healthData")
        serializer.endDocument()
        
        return writer.toString()
    }
    
    // Get file extension for format
    fun getFileExtension(format: String): String {
        return when (format.uppercase()) {
            "CSV" -> "csv"
            "JSON" -> "json"
            "XML" -> "xml"
            "PDF" -> "pdf"
            else -> "txt"
        }
    }
    
    // Get MIME type for format
    fun getMimeType(format: String): String {
        return when (format.uppercase()) {
            "CSV" -> "text/csv"
            "JSON" -> "application/json"
            "XML" -> "text/xml"
            "PDF" -> "application/pdf"
            else -> "text/plain"
        }
    }
}
