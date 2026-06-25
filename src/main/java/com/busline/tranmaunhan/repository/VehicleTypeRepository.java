package com.busline.tranmaunhan.repository;

import com.busline.tranmaunhan.entity.VehicleTypes;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VehicleTypeRepository extends JpaRepository<VehicleTypes, Integer> {

    List<VehicleTypes> findAllByOrderByTypeNameAscIdAsc();
}
