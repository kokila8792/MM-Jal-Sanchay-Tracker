package com.kokila.jalsanchay

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Environment
import androidx.core.content.FileProvider
import com.itextpdf.text.BaseColor
import com.itextpdf.text.Document
import com.itextpdf.text.Font
import com.itextpdf.text.Image
import com.itextpdf.text.Paragraph
import com.itextpdf.text.Phrase
import com.itextpdf.text.pdf.PdfPCell
import com.itextpdf.text.pdf.PdfPTable
import com.itextpdf.text.pdf.PdfWriter
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfGenerator {

    fun createMonthlyPdf(
        context: Context,
        totalWater: Int,
        totalRainfall: Int,
        totalEntries: Int
    ): File {
        lateinit var file: File

        try {

            val document = Document()

             file =
                File(
                    context.getExternalFilesDir(
                        Environment.DIRECTORY_DOCUMENTS
                    ),
                    "JalSanchay_Report.pdf"
                )

            PdfWriter.getInstance(
                document,
                FileOutputStream(file)
            )

            document.open()

            // FONTS

            val titleFont =
                Font(
                    Font.FontFamily.HELVETICA,
                    24f,
                    Font.BOLD,
                    BaseColor.BLUE
                )

            val headingFont =
                Font(
                    Font.FontFamily.HELVETICA,
                    18f,
                    Font.BOLD
                )

            val normalFont =
                Font(
                    Font.FontFamily.HELVETICA,
                    14f,
                    Font.NORMAL
                )

            // TITLE

            val title =
                Paragraph(
                    "Jal-Sanchay Monthly Report",
                    titleFont
                )

            title.spacingAfter = 25f

            document.add(title)

            // DATE

            val currentDate =
                SimpleDateFormat(
                    "dd MMM yyyy",
                    Locale.getDefault()
                ).format(Date())

            val dateText =
                Paragraph(
                    "Generated On: $currentDate",
                    normalFont
                )

            dateText.spacingAfter = 20f

            document.add(dateText)

            // SUMMARY HEADING

            val summaryHeading =
                Paragraph(
                    "Monthly Summary",
                    headingFont
                )

            summaryHeading.spacingAfter = 15f

            document.add(summaryHeading)

            // TABLE

            val table = PdfPTable(2)

            table.widthPercentage = 100f

            table.spacingAfter = 25f

            fun createCell(text: String): PdfPCell {

                val cell =
                    PdfPCell(Phrase(text, normalFont))

                cell.setPadding(12f)

                return cell
            }

            table.addCell(
                createCell("Total Rainfall Entries")
            )

            table.addCell(
                createCell("$totalEntries")
            )

            table.addCell(
                createCell("Total Rainfall")
            )

            table.addCell(
                createCell("$totalRainfall mm")
            )

            table.addCell(
                createCell("Total Water Saved")
            )

            table.addCell(
                createCell("$totalWater Liters")
            )

            val impactDays =
                totalWater / 150

            table.addCell(
                createCell("Estimated Household Usage")
            )

            table.addCell(
                createCell("$impactDays Days")
            )

            document.add(table)

            // IMPACT SECTION

            val impactHeading =
                Paragraph(
                    "Environmental Impact",
                    headingFont
                )

            impactHeading.spacingAfter = 15f

            document.add(impactHeading)

            val co2Reduction =
                (totalWater / 1000) * 2

            val impactText =
                Paragraph(
                    "Your rainwater harvesting system helped conserve $totalWater liters of water and reduced approximately $co2Reduction kg of CO₂ impact.",
                    normalFont
                )

            impactText.spacingAfter = 25f

            document.add(impactText)

            // FOOTER

            val footer =
                Paragraph(
                    "Keep conserving water for a sustainable future 🌱",
                    normalFont
                )

            document.add(footer)

            val chartBitmap =
                try {
                    ChartHolder.chart?.chartBitmap
                } catch (e: Exception) {
                    null
                }

            if (chartBitmap != null) {

                val stream =
                    ByteArrayOutputStream()

                chartBitmap.compress(
                    Bitmap.CompressFormat.PNG,
                    100,
                    stream
                )

                val image =
                    Image.getInstance(
                        stream.toByteArray()
                    )

                image.scaleToFit(
                    500f,
                    300f
                )

                image.spacingBefore = 20f

                document.add(image)
            }

            document.close()


            // OPEN PDF

            val uri =
                FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.provider",
                    file
                )

            val intent =
                Intent(Intent.ACTION_VIEW)

            intent.setDataAndType(
                uri,
                "application/pdf"
            )

            intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION

            context.startActivity(intent)
        } catch (e: Exception) {

            e.printStackTrace()
        }
        return file
    }
}