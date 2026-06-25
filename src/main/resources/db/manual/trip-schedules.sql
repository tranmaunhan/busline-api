CREATE TABLE "TripSchedules" (
    "Id" SERIAL PRIMARY KEY,
    "RouteId" INTEGER NOT NULL REFERENCES "Routes"("Id"),
    "VehicleId" INTEGER NOT NULL REFERENCES "Vehicles"("Id"),
    "DepartureTime" TIME NOT NULL,
    "StartDate" DATE NOT NULL,
    "EndDate" DATE NULL,
    "Status" INTEGER NOT NULL DEFAULT 1 CHECK ("Status" IN (0, 1)),
    "CreatedAt" TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "UpdatedAt" TIMESTAMP WITH TIME ZONE NULL
);

CREATE INDEX "IX_TripSchedules_RouteId"
    ON "TripSchedules" ("RouteId");

CREATE INDEX "IX_TripSchedules_VehicleId"
    ON "TripSchedules" ("VehicleId");

CREATE INDEX "IX_TripSchedules_Status_StartDate_EndDate"
    ON "TripSchedules" ("Status", "StartDate", "EndDate");

COMMENT ON TABLE "TripSchedules"
    IS 'Lich chay mau dung de sinh chuyen xe tu dong theo ngay';

COMMENT ON COLUMN "TripSchedules"."Status"
    IS '1 = dang hoat dong, 0 = ngung hoat dong';
