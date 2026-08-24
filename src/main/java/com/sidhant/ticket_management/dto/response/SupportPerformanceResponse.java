package com.sidhant.ticket_management.dto.response;

public class SupportPerformanceResponse {

    private Long id;
    private String name;
    private long pendingTickets;
    private long attendedTickets;

    public SupportPerformanceResponse(
            Long id,
            String name,
            long pendingTickets,
            long attendedTickets) {

        this.id = id;
        this.name = name;
        this.pendingTickets = pendingTickets;
        this.attendedTickets = attendedTickets;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public long getPendingTickets() {
        return pendingTickets;
    }

    public long getAttendedTickets() {
        return attendedTickets;
    }
}