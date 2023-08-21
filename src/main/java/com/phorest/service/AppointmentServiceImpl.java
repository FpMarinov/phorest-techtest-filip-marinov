package com.phorest.service;

import com.phorest.exception.AppointmentNotFoundException;
import com.phorest.exception.ClientNotFoundException;
import com.phorest.model.csv.AppointmentCsvBean;
import com.phorest.model.entity.Appointment;
import com.phorest.model.entity.Client;
import com.phorest.model.request.AppointmentRequest;
import com.phorest.model.response.AppointmentResponse;
import com.phorest.repository.AppointmentRepository;
import com.phorest.repository.ClientRepository;
import com.phorest.validator.CsvBeanValidator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class AppointmentServiceImpl implements AppointmentService {
  private static final int APPOINTMENT_FILE_PAGE_SIZE = 200;

  private final CsvService csvService;

  private final AppointmentRepository appointmentRepository;
  private final ClientRepository clientRepository;

  private final CsvBeanValidator csvBeanValidator;

  private final ModelMapper modelMapper;

  @Override
  @Transactional
  public void createAppointmentsFromFile(@NonNull MultipartFile file) {
    int currentPageNumber = 0;
    Page<AppointmentCsvBean> currentAppointmentPage;
    boolean isCurrentPageLast;

    do {
      currentAppointmentPage =
          csvService.getElementsFromCsvFile(
              file, AppointmentCsvBean.class, currentPageNumber, APPOINTMENT_FILE_PAGE_SIZE);

      createAppointmentsInPage(currentAppointmentPage);

      isCurrentPageLast = currentAppointmentPage.isLast();

      currentPageNumber++;
    } while (!isCurrentPageLast);
  }

  private void createAppointmentsInPage(Page<AppointmentCsvBean> appointmentPage) {
    List<AppointmentCsvBean> appointmentCsvBeans = appointmentPage.getContent();

    Set<UUID> clientIds =
        appointmentCsvBeans.stream()
            .map(AppointmentCsvBean::getClientId)
            .collect(Collectors.toSet());

    Map<UUID, Client> clientsById =
        clientRepository.findByIdIn(clientIds).stream()
            .collect(Collectors.toMap(Client::getId, Function.identity()));

    List<Appointment> appointments =
        appointmentCsvBeans.stream()
            .peek(csvBeanValidator::validateCsvBean)
            .map(csvBean -> csvBeanToEntity(csvBean, clientsById))
            .toList();

    appointmentRepository.saveAllAndFlush(appointments);
  }

  private Appointment csvBeanToEntity(
      @NonNull AppointmentCsvBean csvBean, @NonNull Map<UUID, Client> clientsById) {

    Appointment appointment = modelMapper.map(csvBean, Appointment.class);

    Client client = clientsById.get(csvBean.getClientId());

    if (client == null) {
      throw new ClientNotFoundException(csvBean.getClientId());
    }

    client.addAppointment(appointment);

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
