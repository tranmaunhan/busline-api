package com.busline.tranmaunhan.repository;

import com.busline.tranmaunhan.entity.Locations;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LocationsRepository extends JpaRepository<Locations, Integer> {

    List<Locations> findAllByOrderByNameAsc();
}
