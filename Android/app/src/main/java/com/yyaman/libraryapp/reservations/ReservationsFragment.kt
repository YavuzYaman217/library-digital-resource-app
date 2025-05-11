package com.yyaman.libraryapp.reservations

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.View as AndroidView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.yyaman.libraryapp.adapter.ReservationAdapter
import com.yyaman.libraryapp.adapter.ReservationAdapter.ReservationItem
import com.yyaman.libraryapp.databinding.FragmentReservationsBinding
import com.yyaman.libraryapp.notifications.OverdueWorker
import java.time.*
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import android.util.Log

class ReservationsFragment : Fragment() {

    private var _binding: FragmentReservationsBinding? = null
    private val binding get() = _binding!!

    private val vm: ReservationViewModel by viewModels()
    private lateinit var adapter: ReservationAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReservationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1) Adapter with cancel support
        adapter = ReservationAdapter(
            onCancel = { resId ->
                vm.cancelReservation(resId) { ok, msg ->
                    Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                    if (ok) vm.loadReservations()
                }
            },
            showCancel = true
        )

        // 2) RecyclerView
        binding.rvReservations.layoutManager = LinearLayoutManager(requireContext())
        binding.rvReservations.adapter = adapter

        // 3) Observe state
        vm.state.observe(viewLifecycleOwner) { st ->
            // spinner
            binding.progressBar.visibility =
                if (st is ReservationState.Loading) AndroidView.VISIBLE else AndroidView.GONE

            when (st) {
                is ReservationState.Success -> {
                    val displayFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                    val isoOffsetFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME
                    val isoLocalFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

                    val parsedItems = st.reservations.map { resp ->
                        // Parse and format reservation date
                        val rawReservedDate = resp.createdAt
                        val formattedReservedDate = parseAndFormatDateTime(rawReservedDate, displayFmt)

                        // Parse and format due date, also schedule notification
                        val rawDueDate = resp.dueDate
                        val formattedDueDate = parseAndFormatDateTime(rawDueDate, displayFmt)

                        // Schedule notification for books not yet overdue
                        scheduleOverdueNotification(resp.id, resp.book.title, rawDueDate)

                        ReservationItem(
                            id = resp.id,
                            book = resp.book,
                            reservedAt = formattedReservedDate,
                            dueDate = formattedDueDate
                        )
                    }

                    adapter.submitList(parsedItems)
                    binding.tvEmpty.visibility =
                        if (parsedItems.isEmpty()) AndroidView.VISIBLE else AndroidView.GONE
                }

                is ReservationState.Error -> {
                    Toast.makeText(requireContext(), st.error, Toast.LENGTH_LONG).show()
                }

                // Loading already handled by spinner toggle above
                else -> { }
            }
        }

        // 4) Fire off initial load
        vm.loadReservations()
    }

    /**
     * Parse a date-time string using multiple formats and return a formatted string
     */
    private fun parseAndFormatDateTime(rawDate: String, outputFormatter: DateTimeFormatter): String {
        return try {
            // First try parsing as OffsetDateTime
            try {
                val odt = OffsetDateTime.parse(rawDate)
                odt.format(outputFormatter)
            } catch (e1: DateTimeParseException) {
                Log.d("RVFrag", "Offset parse failed for '$rawDate', trying ISO_DATE_TIME", e1)
                // Then try as OffsetDateTime with explicit formatter
                try {
                    val odt = OffsetDateTime.parse(rawDate, DateTimeFormatter.ISO_DATE_TIME)
                    odt.format(outputFormatter)
                } catch (e2: DateTimeParseException) {
                    Log.d("RVFrag", "ISO_DATE_TIME parse failed for '$rawDate', trying LocalDateTime", e2)
                    // Then try as LocalDateTime
                    try {
                        val ldt = LocalDateTime.parse(rawDate)
                        ldt.format(outputFormatter)
                    } catch (e3: DateTimeParseException) {
                        Log.d("RVFrag", "Default LocalDateTime parse failed for '$rawDate', trying with formatter", e3)
                        // Finally try with explicit formatter for LocalDateTime
                        try {
                            val ldt = LocalDateTime.parse(rawDate, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                            ldt.format(outputFormatter)
                        } catch (e4: Exception) {
                            Log.e("RVFrag", "All parsing attempts failed for '$rawDate'", e4)
                            // fallback to raw
                            rawDate
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("RVFrag", "Unexpected error parsing date '$rawDate'", e)
            rawDate
        }
    }

    /**
     * Schedule an overdue notification for a book reservation
     */
    private fun scheduleOverdueNotification(reservationId: Int, bookTitle: String, rawDueDate: String) {
        try {
            // Parse the due date to get the milliseconds
            val dueDateTime = try {
                // Try various ways to parse the date
                try {
                    ZonedDateTime.parse(rawDueDate).toInstant()
                } catch (e1: DateTimeParseException) {
                    try {
                        OffsetDateTime.parse(rawDueDate).toInstant()
                    } catch (e2: DateTimeParseException) {
                        try {
                            LocalDateTime.parse(rawDueDate, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                                .atZone(ZoneId.systemDefault()).toInstant()
                        } catch (e3: DateTimeParseException) {
                            LocalDateTime.parse(rawDueDate)
                                .atZone(ZoneId.systemDefault()).toInstant()
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("RVFrag", "Failed to parse due date for notification: $rawDueDate", e)
                return
            }

            // Schedule the notification
            val dueMillis = dueDateTime.toEpochMilli()
            context?.let { ctx ->
                OverdueWorker.schedule(
                    ctx = ctx,
                    reservationId = reservationId,
                    bookTitle = bookTitle,
                    dueMillis = dueMillis
                )
                Log.d("RVFrag", "Scheduled overdue notification for book: $bookTitle, due: $rawDueDate")
            }
        } catch (e: Exception) {
            Log.e("RVFrag", "Error scheduling notification", e)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}