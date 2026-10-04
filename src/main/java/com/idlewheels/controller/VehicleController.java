package com.idlewheels.controller;

import com.idlewheels.dto.VehicleForm;
import com.idlewheels.model.Bike;
import com.idlewheels.model.Car;
import com.idlewheels.model.Vehicle;
import com.idlewheels.service.VehicleService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class VehicleController {
    private final VehicleService vehicleService;

    public VehicleController(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    @GetMapping("/owner/vehicles")
    public String showVehicles(@SessionAttribute("userId") Long ownerId,
                               @RequestParam(defaultValue = "false") boolean added,
                               @RequestParam(defaultValue = "false") boolean updated,
                               @RequestParam(defaultValue = "false") boolean deleted,
                               Model model) {
        addPageData(ownerId, model);
        model.addAttribute("vehicleForm", new VehicleForm());
        model.addAttribute("vehicleAdded", added);
        model.addAttribute("vehicleUpdated", updated);
        model.addAttribute("vehicleDeleted", deleted);
        model.addAttribute("vehicleFormAction", "/owner/vehicles");
        model.addAttribute("editMode", false);
        return "owner/vehicles";
    }

    @PostMapping("/owner/vehicles")
    public String addVehicle(@SessionAttribute("userId") Long ownerId,
                             @Valid @ModelAttribute("vehicleForm") VehicleForm form,
                             BindingResult bindingResult, Model model,
                             RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            addPageData(ownerId, model);
            model.addAttribute("vehicleFormAction", "/owner/vehicles");
            model.addAttribute("editMode", false);
            return "owner/vehicles";
        }
        try {
            vehicleService.addVehicle(ownerId, form);
            redirectAttributes.addAttribute("added", true);
            return "redirect:/owner/vehicles";
        } catch (IllegalArgumentException exception) {
            addPageData(ownerId, model);
            model.addAttribute("vehicleFormAction", "/owner/vehicles");
            model.addAttribute("editMode", false);
            model.addAttribute("vehicleError", exception.getMessage());
            return "owner/vehicles";
        }
    }

    @GetMapping("/owner/vehicles/{vehicleId}/edit")
    public String editVehicleForm(@SessionAttribute("userId") Long ownerId,
                                  @PathVariable Long vehicleId, Model model) {
        Vehicle vehicle = vehicleService.findOwnedVehicle(ownerId, vehicleId);
        addPageData(ownerId, model);
        model.addAttribute("vehicleForm", toForm(vehicle));
        model.addAttribute("vehicleFormAction", "/owner/vehicles/" + vehicleId + "/edit");
        model.addAttribute("editingVehicle", vehicle);
        model.addAttribute("editMode", true);
        return "owner/vehicles";
    }

    @PostMapping("/owner/vehicles/{vehicleId}/edit")
    public String updateVehicle(@SessionAttribute("userId") Long ownerId,
                                @PathVariable Long vehicleId,
                                @Valid @ModelAttribute("vehicleForm") VehicleForm form,
                                BindingResult bindingResult, Model model,
                                RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            showEditErrors(ownerId, vehicleId, form, model);
            return "owner/vehicles";
        }
        try {
            vehicleService.updateVehicle(ownerId, vehicleId, form);
            redirectAttributes.addAttribute("updated", true);
            return "redirect:/owner/vehicles";
        } catch (IllegalArgumentException exception) {
            showEditErrors(ownerId, vehicleId, form, model);
            model.addAttribute("vehicleError", exception.getMessage());
            return "owner/vehicles";
        }
    }

    @PostMapping("/owner/vehicles/{vehicleId}/delete")
    public String deleteVehicle(@SessionAttribute("userId") Long ownerId,
                                @PathVariable Long vehicleId,
                                RedirectAttributes redirectAttributes) {
        vehicleService.deleteVehicle(ownerId, vehicleId);
        redirectAttributes.addAttribute("deleted", true);
        return "redirect:/owner/vehicles";
    }

    private void showEditErrors(Long ownerId, Long vehicleId, VehicleForm form, Model model) {
        addPageData(ownerId, model);
        model.addAttribute("vehicleForm", form);
        model.addAttribute("vehicleFormAction", "/owner/vehicles/" + vehicleId + "/edit");
        model.addAttribute("editingVehicle", vehicleService.findOwnedVehicle(ownerId, vehicleId));
        model.addAttribute("editMode", true);
    }

    private VehicleForm toForm(Vehicle vehicle) {
        VehicleForm form = new VehicleForm();
        form.setVehicleType(vehicle.getVehicleType());
        form.setMake(vehicle.getMake());
        form.setModel(vehicle.getModel());
        form.setManufactureYear(vehicle.getManufactureYear());
        if (vehicle instanceof Car car) {
            form.setSeats((int) car.getSeats());
            form.setTransmission(car.getTransmission());
        } else if (vehicle instanceof Bike bike) {
            form.setEngineCc(bike.getEngineCc());
        }
        return form;
    }

    private void addPageData(Long ownerId, Model model) {
        model.addAttribute("vehicles", vehicleService.findOwnerVehicles(ownerId));
    }
}
