package com.phorest.service;

import com.phorest.exception.AppointmentNotFoundException;
import com.phorest.exception.ClientNotFoundException;
import com.phorest.model.csv.AppointmentCsvBean;
import com.phorest.model.entity.Appointment;
import com.phorest.model.request.AppointmentRequest;
import com.phorest.model.response.AppointmentResponse;
import com.phorest.repository.AppointmentRepository;
import com.phorest.repository.ClientRepository;
import com.phorest.validator.CsvBeanValidator;
import java.util.List;
import java.util.UUID;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class AppointmentServiceImpl implements AppointmentService {
  private final CsvService csvService;

  private final AppointmentRepository appointmentRepository;
  private final ClientRepository clientRepository;

  private final CsvBeanValidator csvBeanValidator;

  private final ModelMapper modelMapper;

  @Override
  @Transactional
  public void createAppointmentsFromFile(@NonNull MultipartFile file) {
    List<Appointment> appointments =
        csvService.getElementsFromCsvFile(file, AppointmentCsvBean.class).stream()
            .peek(csvBeanValidator::validateCsvBean)
            .map(this::csvBeanToEntity)
            .toList();

    appointmentRepository.saveAll(appointments);
  }

  private Appointment csvBeanToEntity(@NonNull AppointmentCsvBean csvBean) {
    Appointment appointment = modelMapper.map(csvBean, Appointment.class);

    clientRepository
        .findById(csvBean.getClientId())
        .ifPresentOrElse(
            client -> client.addAppointment(appointment),
            () -> {
              throw new ClientNotFoundException(csvBean.getClientId());
            });

    return appointment;
  }

  @Override
  @Transactional
  public AppointmentResponse updateAppointment(
      @NonNull UUID appointmentId, @NonNull AppointmentRequest appointmentRequest) {
    Appointment appointment = getAppointment(appointmentId);

    modelMapper.map(appointmentRequest, appointment);

    return modelMapper.map(appointment, AppointmentResponse.class);
  }

  @Override
  @Transactional
  public void deleteAppointment(@NonNull UUID appointmentId) {
    Appointment appointment = getAppointment(appointmentId);

    appointmentRepository.delete(appointment);
  }

  @Override
  @Transactional(readOnly = true)
  public AppointmentResponse getAppointmentResponse(@NonNull UUID appointmentId) {
    Appointment appointment = getAppointment(appointmentId);

    return modelMapper.map(appointment, AppointmentResponse.class);
  }

  private Appointment getAppointment(@NonNull UUID appointmentId) {
    return appointmentRepository
        .findById(appointmentId)
        .orElseThrow(() -> new AppointmentNotFoundException(appointmentId));
  }
}
