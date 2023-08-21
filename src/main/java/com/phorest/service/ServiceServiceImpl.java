package com.phorest.service;

import com.phorest.exception.AppointmentNotFoundException;
import com.phorest.exception.ServiceNotFoundException;
import com.phorest.model.csv.ServiceCsvBean;
import com.phorest.model.entity.Appointment;
import com.phorest.model.entity.Service;
import com.phorest.model.request.ServiceRequest;
import com.phorest.model.response.ServiceResponse;
import com.phorest.repository.AppointmentRepository;
import com.phorest.repository.ServiceRepository;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@org.springframework.stereotype.Service
public class ServiceServiceImpl implements ServiceService {
  private static final int SERVICE_FILE_PAGE_SIZE = 200;

  private final CsvService csvService;

  private final ServiceRepository serviceRepository;
  private final AppointmentRepository appointmentRepository;
  private final CsvBeanValidator csvBeanValidator;

  private final ModelMapper modelMapper;

  @Override
  @Transactional
  public void createServicesFromFile(@NonNull MultipartFile file) {
    int currentPageNumber = 0;
    Page<ServiceCsvBean> currentServicePage;
    boolean isCurrentPageLast;

    do {
      currentServicePage =
          csvService.getElementsFromCsvFile(
              file, ServiceCsvBean.class, currentPageNumber, SERVICE_FILE_PAGE_SIZE);

      createServicesInPage(currentServicePage);

      isCurrentPageLast = currentServicePage.isLast();

      currentPageNumber++;
    } while (!isCurrentPageLast);
  }

  private void createServicesInPage(Page<ServiceCsvBean> servicePage) {
    List<ServiceCsvBean> serviceCsvBeans = servicePage.getContent();

    Set<UUID> appointmentIds =
        serviceCsvBeans.stream().map(ServiceCsvBean::getAppointmentId).collect(Collectors.toSet());

    Map<UUID, Appointment> appointmentsById =
        appointmentRepository.findByIdIn(appointmentIds).stream()
            .collect(Collectors.toMap(Appointment::getId, Function.identity()));

    List<Service> services =
        serviceCsvBeans.stream()
            .peek(csvBeanValidator::validateCsvBean)
            .map(csvBean -> csvBeanToEntity(csvBean, appointmentsById))
            .toList();

    serviceRepository.saveAllAndFlush(services);
  }

  private Service csvBeanToEntity(
      @NonNull ServiceCsvBean csvBean, @NonNull Map<UUID, Appointment> appointmentsById) {

    Service service = modelMapper.map(csvBean, Service.class);

    Appointment appointment = appointmentsById.get(csvBean.getAppointmentId());

    if (appointment == null) {
      throw new AppointmentNotFoundException(csvBean.getAppointmentId());
    }

    appointment.addService(service);

    return service;
  }

  @Override
  @Transactional
  public ServiceResponse updateService(
      @NonNull UUID serviceId, @NonNull ServiceRequest serviceRequest) {
    Service service = getService(serviceId);

    modelMapper.map(serviceRequest, service);

    return modelMapper.map(service, ServiceResponse.class);
  }

  @Override
  @Transactional
  public void deleteService(@NonNull UUID serviceId) {
    Service service = getService(serviceId);

    serviceRepository.delete(service);
  }

  @Override
  @Transactional(readOnly = true)
  public ServiceResponse getServiceResponse(@NonNull UUID serviceId) {
    Service service = getService(serviceId);

    return modelMapper.map(service, ServiceResponse.class);
  }

  private Service getService(@NonNull UUID serviceId) {
    return serviceRepository
        .findById(serviceId)
        .orElseThrow(() -> new ServiceNotFoundException(serviceId));
  }
}
