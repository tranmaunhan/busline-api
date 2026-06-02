package com.busline.tranmaunhan.repository;

import com.busline.tranmaunhan.entity.Tickets;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketRepository extends JpaRepository<Tickets, Integer> {
}
