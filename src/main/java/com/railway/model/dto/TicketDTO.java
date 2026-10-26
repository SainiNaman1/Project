package com.railway.model.dto;

import com.railway.model.Reservation;
import com.railway.model.ReservationPassenger;
import java.time.format.DateTimeFormatter;

/**
 * Data Transfer Object (DTO) formatted for ticket presentation, print preview,
 * and text export.
 */
public class TicketDTO {
    private final Reservation reservation;

    public TicketDTO(Reservation reservation) {
        this.reservation = reservation;
    }

    public Reservation getReservation() {
        return reservation;
    }

    /**
     * Generates an official ASCII-formatted ticket preview suitable for viewing
     * in the UI or exporting as text.
     */
    public String generatePrintableText() {
        StringBuilder sb = new StringBuilder();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        sb.append("================================================================================\n");
        sb.append("                       INDIAN RAILWAYS SIMULATION SYSTEM                        \n");
        sb.append("                             ELECTRONIC TICKET                                  \n");
        sb.append("================================================================================\n");
        sb.append(String.format(" PNR NUMBER: %-16s STATUS: %-15s DATE: %s\n", 
                reservation.getPnrNumber(), 
                reservation.getStatus(), 
                reservation.getJourneyDate().format(dateFormatter)));
        sb.append("--------------------------------------------------------------------------------\n");
        sb.append(String.format(" TRAIN: %-6s %-25s CLASS: %s\n", 
                reservation.getTrain() != null ? reservation.getTrain().getTrainNumber() : String.valueOf(reservation.getTrainId()),
                reservation.getTrain() != null ? reservation.getTrain().getTrainName() : "Express",
                reservation.getTravelClass().getDisplayName()));
        sb.append(String.format(" FROM : %-25s TO: %s\n", 
                reservation.getFromStation() != null ? reservation.getFromStation().toString() : "Origin",
                reservation.getToStation() != null ? reservation.getToStation().toString() : "Destination"));
        sb.append("--------------------------------------------------------------------------------\n");
        sb.append(String.format(" %-4s | %-25s | %-4s | %-6s | %-6s | %-12s\n", "S.No", "Passenger Name", "Age", "Gender", "Coach", "Berth"));
        sb.append("--------------------------------------------------------------------------------\n");
        
        int sNo = 1;
        for (ReservationPassenger p : reservation.getPassengers()) {
            sb.append(String.format(" %-4d | %-25s | %-4d | %-6s | %-6s | %-4d (%s)\n",
                    sNo++,
                    p.getPassengerName(),
                    p.getAge(),
                    p.getGender().getLabel(),
                    p.getAssignedCoach(),
                    p.getAssignedSeatNumber(),
                    p.getAssignedBerth().getLabel()));
        }
        sb.append("--------------------------------------------------------------------------------\n");
        sb.append(String.format(" TOTAL FARE: INR %-15s (INCLUSIVE OF GST & RESERVATION FEE)\n", reservation.getTotalFare()));
        sb.append(String.format(" BOOKED ON : %s\n", reservation.getBookingTimestamp() != null ? reservation.getBookingTimestamp().format(DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm:ss")) : "N/A"));
        sb.append("================================================================================\n");
        sb.append(" Note: This is an educational simulation document. Valid ID required during travel.\n");
        sb.append("================================================================================\n");

        return sb.toString();
    }
}
