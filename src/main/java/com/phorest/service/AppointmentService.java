package com.phorest.service;

import com.phorest.model.request.AppointmentRequest;
import com.phorest.model.response.AppointmentResponse;
import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;

public interface AppointmentService {
  void createAppointmentsFromFile(MultipartFile file);

  AppointmentResponse updateAppointment(UUID appointmentId, AppointmentRequest appointmentRequest);

  void deleteAppointment(UUID appointmentId);

  AppointmentResponse getAppointmentResponse(UUID appointmentId);
}
