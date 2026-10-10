package com.navira.app.data.export

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.navira.app.data.local.entity.LocationPointEntity
import com.navira.app.data.local.entity.TripEntity
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object GpxExporter {

    private val isoDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    /**
     * Generates a standard GPX 1.1 XML string for a trip and its GPS points.
     */
    fun buildGpxXml(trip: TripEntity, points: List<LocationPointEntity>): String {
        val sb = StringBuilder()
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
        sb.append("<gpx version=\"1.1\" creator=\"Navira - Travel Tracker Android\" ")
        sb.append("xmlns=\"http://www.topografix.com/GPX/1/1\" ")
        sb.append("xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" ")
        sb.append("xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd\">\n")

        sb.append("  <metadata>\n")
        sb.append("    <name>${escapeXml(trip.title)}</name>\n")
        sb.append("    <time>${isoDateFormat.format(Date(trip.startTime))}</time>\n")
        sb.append("  </metadata>\n")

        sb.append("  <trk>\n")
        sb.append("    <name>${escapeXml(trip.title)}</name>\n")
        sb.append("    <type>${trip.mode.name}</type>\n")

        var openSegment = false
        for (point in points) {
            if (point.isSegmentStart && openSegment) {
                sb.append("    </trkseg>\n")
                openSegment = false
            }
            if (!openSegment) {
                sb.append("    <trkseg>\n")
                openSegment = true
            }

            sb.append("      <trkpt lat=\"${point.latitude}\" lon=\"${point.longitude}\">\n")
            if (point.altitude != 0.0) {
                sb.append("        <ele>${point.altitude}</ele>\n")
            }
            sb.append("        <time>${isoDateFormat.format(Date(point.timestamp))}</time>\n")
            if (point.speed > 0f) {
                sb.append("        <speed>${point.speed}</speed>\n")
            }
            sb.append("      </trkpt>\n")
        }

        if (openSegment) {
            sb.append("    </trkseg>\n")
        }

        sb.append("  </trk>\n")
        sb.append("</gpx>")
        return sb.toString()
    }

    /**
     * Exports GPX to cache/exports directory and creates a share intent.
     */
    fun exportAndShareGpx(
        context: Context,
        trip: TripEntity,
        points: List<LocationPointEntity>
    ): Intent {
        val xml = buildGpxXml(trip, points)
        val exportDir = File(context.cacheDir, "gpx_exports").apply { mkdirs() }
        val sanitizedTitle = trip.title.replace("[^a-zA-Z0-9_-]".toRegex(), "_")
        val file = File(exportDir, "Navira_${sanitizedTitle}_${trip.id.take(6)}.gpx")

        FileWriter(file).use { writer ->
            writer.write(xml)
        }

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        return Intent(Intent.ACTION_SEND).apply {
            type = "application/gpx+xml"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, trip.title)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    private fun escapeXml(str: String): String {
        return str.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
    }
}
