package com.phorest.controller;

import com.phorest.model.request.AppointmentRequest;
import com.phorest.model.response.AppointmentResponse;
import com.phorest.service.AppointmentService;
import com.phorest.util.PrincipalUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "Appointment Operations")
public class AppointmentController {
  private final AppointmentService appointmentService;

  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping(
      path = "/appointments/files",
      consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Create new Appointments by uploading a csv file")
  public void createAppointmentsFromFile(
      @RequestParam("file") MultipartFile file, Principal principal) {
    log.info(
        "[APPOINTMENTS] Request from {} to create new appointments from csv file",
        PrincipalUtils.getPrincipalName(principal));

    appointmentService.createAppointmentsFromFile(file);
  }

  @ResponseStatus(HttpStatus.OK)
  @PutMapping(
      path = "/appointments/{appointmentId}",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Update Appointment")
  public AppointmentResponse updateAppointment(
      @PathVariable UUID appointmentId,
      @Valid @RequestBody AppointmentRequest appointmentRequest,
      Principal principal) {
    log.info(
        "[APPOINTMENTS] Request from {} to update appointment with id: {}",
        PrincipalUtils.getPrincipalName(principal),
        appointmentId);

    return appointmentService.updateAppointment(appointmentId, appointmentRequest);
  }

  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping(path = "/appointments/{appointmentId}")
  @Operation(summary = "Delete Appointment")
  public void deleteAppointment(@PathVariable UUID appointmentId, Principal principal) {
    log.info(
        "[APPOINTMENTS] Request from {} to delete appointment with id: {}",
        PrincipalUtils.getPrincipalName(principal),
        appointmentId);

    appointmentService.deleteAppointment(appointmentId);
  }

  @ResponseStatus(HttpStatus.OK)
  @GetMapping(path = "/appointments/{appointmentId}", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Get Appointment")
  public AppointmentResponse getAppointment(@PathVariable UUID appointmentId, Principal principal) {
    log.info(
        "[APPOINTMENTS] Request from {} to get appointment with id: {}",
        PrincipalUtils.getPrincipalName(principal),
        appointmentId);

    return appointmentService.getAppointmentResponse(appointmentId);
  }
}
