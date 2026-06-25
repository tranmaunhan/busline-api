package com.busline.tranmaunhan.repository;

import com.busline.tranmaunhan.entity.Vehicles;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface VehicleRepository extends JpaRepository<Vehicles, Integer> {

    boolean existsByLicensePlateIgnoreCase(String licensePlate);

    boolean existsByLicensePlateIgnoreCaseAndIdNot(String licensePlate, Integer id);

    @Query("""
            SELECT
                vehicle.id AS vehicleId,
                vehicle.licensePlate AS licensePlate,
                vehicle.brand AS brand,
                vehicle.manufactureYear AS manufactureYear,
                vehicle.status AS status,
                vehicleType.id AS vehicleTypeId,
                vehicleType.typeName AS vehicleTypeName,
                vehicleType.totalSeats AS totalSeats
            FROM Vehicles vehicle
            LEFT JOIN vehicle.vehicleType vehicleType
            ORDER BY vehicle.licensePlate ASC, vehicle.id ASC
            """)
    List<AdminVehicleProjection> findAdminVehicles();

    @Query("""
            SELECT
                vehicle.id AS vehicleId,
                vehicle.licensePlate AS licensePlate,
                vehicle.brand AS brand,
                vehicle.manufactureYear AS manufactureYear,
                vehicle.status AS status,
                vehicleType.id AS vehicleTypeId,
                vehicleType.typeName AS vehicleTypeName,
                vehicleType.totalSeats AS totalSeats
            FROM Vehicles vehicle
            LEFT JOIN vehicle.vehicleType vehicleType
            WHERE vehicle.id = :vehicleId
            """)
    AdminVehicleProjection findAdminVehicleById(@Param("vehicleId") Integer vehicleId);

    interface AdminVehicleProjection {
        Integer getVehicleId();

        String getLicensePlate();

        String getBrand();

        Integer getManufactureYear();

        String getStatus();

        Integer getVehicleTypeId();

        String getVehicleTypeName();

        Integer getTotalSeats();
    }
}
