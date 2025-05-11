// app/src/main/java/com/yyaman/libraryapp/adapter/ReservationAdapter.kt
package com.yyaman.libraryapp.adapter

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.yyaman.libraryapp.data.Book
import com.yyaman.libraryapp.databinding.ItemReservationBinding
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

/**
 * @param onCancel    invoked with the reservationId when the user taps Cancel
 * @param showCancel  if false, hides the Cancel button entirely
 */
class ReservationAdapter(
    private val onCancel: (reservationId: Int) -> Unit,
    private val showCancel: Boolean = true
) : RecyclerView.Adapter<ReservationAdapter.VH>() {

    /** A single row's data: reservationId + book + reservation & due dates */
    data class ReservationItem(
        val id: Int,
        val book: Book,
        val reservedAt: String,
        val dueDate: String
    )

    private var items: List<ReservationItem> = emptyList()

    /** Replace the list whenever new data arrives */
    fun submitList(newList: List<ReservationItem>) {
        items = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemReservationBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return VH(binding)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(items[position])
    }

    inner class VH(private val b: ItemReservationBinding) :
        RecyclerView.ViewHolder(b.root) {

        // Keep multiple formatters for flexible parsing
        private val isoOffsetFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME
        private val isoLocalFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
        private val displayFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

        fun bind(item: ReservationItem) {
            b.tvTitle.text = item.book.title
            b.tvAuthor.text = item.book.author
            b.tvYear.text = item.book.year.toString()
            b.tvReservedAt.text = "Reserved: ${item.reservedAt}"
            b.tvDueDate.text = "Due: ${item.dueDate}"

            // Try to parse due date with multiple fallbacks
            val isPastDue = try {
                // First try parsing as ZonedDateTime with offset
                try {
                    val due = ZonedDateTime.parse(item.dueDate, isoOffsetFormatter)
                    due.isBefore(ZonedDateTime.now())
                } catch (e1: DateTimeParseException) {
                    Log.d("ReservationAdapter", "Failed to parse with ISO_OFFSET_DATE_TIME: ${e1.message}")

                    // Then try parsing as LocalDateTime and convert to ZonedDateTime
                    try {
                        val localDue = LocalDateTime.parse(item.dueDate, isoLocalFormatter)
                        val zonedDue = localDue.atZone(ZoneId.systemDefault())
                        zonedDue.isBefore(ZonedDateTime.now())
                    } catch (e2: DateTimeParseException) {
                        Log.d("ReservationAdapter", "Failed to parse with ISO_LOCAL_DATE_TIME: ${e2.message}")

                        // If it's already in display format, try parsing that
                        try {
                            val localDue = LocalDateTime.parse(item.dueDate, displayFormatter)
                            val zonedDue = localDue.atZone(ZoneId.systemDefault())
                            zonedDue.isBefore(ZonedDateTime.now())
                        } catch (e3: DateTimeParseException) {
                            Log.e("ReservationAdapter", "All parsing attempts failed for '${item.dueDate}'", e3)
                            // Default to false if we can't parse the date
                            false
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("ReservationAdapter", "Unexpected error parsing date", e)
                false
            }

            // Set text color based on whether it's past due
            b.tvDueDate.setTextColor(if (isPastDue) Color.RED else Color.DKGRAY)

            // Handle cancel button visibility and click
            if (showCancel) {
                b.btnCancel.visibility = View.VISIBLE
                b.btnCancel.setOnClickListener { onCancel(item.id) }
            } else {
                b.btnCancel.visibility = View.GONE
            }
        }
    }
}