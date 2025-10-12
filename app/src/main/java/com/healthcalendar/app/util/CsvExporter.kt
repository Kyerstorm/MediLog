package com.healthcalendar.app.util

import com.healthcalendar.app.data.database.entities.Appointment
import com.healthcalendar.app.data.database.entities.Medication
import com.healthcalendar.app.data.database.entities.MedicationLog

object CsvExporter {
    
    fun exportAppointments(appointments: List<Appointment>): String {
        val header = "ID,Title,Description,Category,Start Time,End Time,Location,Color\n"
        val rows = appointments.joinToString("\n") { appointment ->
            "${appointment.id},${csvEscape(appointment.title)},${csvEscape(appointment.description)},${appointment.category},${appointment.startTime},${appointment.endTime},${csvEscape(appointment.location)},${appointment.color}"
        }
        return header + rows
    }
    
    fun exportMedications(medications: List<Medication>): String {
        val header = "ID,Name,Dosage,Amount,Unit,Form,Instructions,Notes,Photo URI,Created At,Is Active\n"
        val rows = medications.joinToString("\n") { medication ->
            "${medication.id},${csvEscape(medication.name)},${medication.dosage},${medication.amount},${medication.unit},${medication.form},${csvEscape(medication.instructions)},${csvEscape(medication.notes)},${csvEscape(medication.photoUri)},${medication.createdAt},${medication.isActive}"
        }
        return header + rows
    }
    
    fun exportMedicationLogs(logs: List<MedicationLog>): String {
        val header = "ID,Medication ID,Scheduled Time,Taken Time,Status,Notes\n"
        val rows = logs.joinToString("\n") { log ->
            "${log.id},${log.medicationId},${log.scheduledTime},${log.takenTime ?: ""},${log.status},${csvEscape(log.notes)}"
        }
        return header + rows
    }
    
    fun exportAll(
        appointments: List<Appointment>,
        medications: List<Medication>,
        logs: List<MedicationLog>
    ): String {
        return """
            |=== APPOINTMENTS ===
            |${exportAppointments(appointments)}
            |
            |=== MEDICATIONS ===
            |${exportMedications(medications)}
            |
            |=== MEDICATION LOGS ===
            |${exportMedicationLogs(logs)}
        """.trimMargin()
    }
    
    private fun csvEscape(value: String?): String {
        if (value == null) return ""
        return if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            "\"${value.replace("\"", "\"\"")}\""
        } else {
            value
        }
    }
}
