CREATE TABLE "RouteSegmentPrices" (
    "Id" SERIAL PRIMARY KEY,
    "RouteId" INTEGER NOT NULL REFERENCES "Routes"("Id"),
    "PickupStopId" INTEGER NOT NULL REFERENCES "RouteStops"("Id"),
    "DropoffStopId" INTEGER NOT NULL REFERENCES "RouteStops"("Id"),
    "Price" NUMERIC(12, 2) NOT NULL CHECK ("Price" >= 0),
    CONSTRAINT "UK_RouteSegmentPrices_Route_StopPair"
        UNIQUE ("RouteId", "PickupStopId", "DropoffStopId")
);

CREATE INDEX "IX_RouteSegmentPrices_RouteId"
    ON "RouteSegmentPrices" ("RouteId");

CREATE INDEX "IX_RouteSegmentPrices_PickupStopId"
    ON "RouteSegmentPrices" ("PickupStopId");

CREATE INDEX "IX_RouteSegmentPrices_DropoffStopId"
    ON "RouteSegmentPrices" ("DropoffStopId");

COMMENT ON TABLE "RouteSegmentPrices"
    IS 'Bang gia co dinh theo tung cap diem don/diem tra trong cung mot tuyen';

COMMENT ON COLUMN "RouteSegmentPrices"."Price"
    IS 'Gia cua chang, se duoc gan vao Tickets.Price khi tao ve';
