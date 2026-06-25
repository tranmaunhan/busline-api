package com.busline.tranmaunhan.repository;

import com.busline.tranmaunhan.entity.SeatTemplates;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SeatTemplateRepository extends JpaRepository<SeatTemplates, Integer> {

    List<SeatTemplates> findAllByVehicleTypeIdOrderByDeckAscRowIndexAscColIndexAscSeatCodeAsc(Integer vehicleTypeId);
}
