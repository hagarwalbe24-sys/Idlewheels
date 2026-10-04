package com.idlewheels.service.filter;

import com.idlewheels.model.Listing;
import com.idlewheels.model.VehicleType;

public class VehicleTypeFilter implements SearchFilter {
    private final VehicleType vehicleType;

    public VehicleTypeFilter(VehicleType vehicleType) {
        this.vehicleType = vehicleType;
    }

    @Override
    public boolean matches(Listing listing) {
        return listing.getVehicle().getVehicleType() == vehicleType;
    }
}
