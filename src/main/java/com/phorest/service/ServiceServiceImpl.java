package com.phorest.service;

import com.phorest.exception.AppointmentNotFoundException;
import com.phorest.exception.ServiceNotFoundException;
import com.phorest.model.csv.ServiceCsvBean;
import com.phorest.model.entity.Service;
import com.phorest.model.request.ServiceRequest;
import com.phorest.model.response.ServiceResponse;
import com.phorest.repository.AppointmentRepository;
import com.phorest.repository.ServiceRepository;
import com.phorest.validator.CsvBeanValidator;
import java.util.List;
import java.util.UUID;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@org.springframework.stereotype.Service
public class ServiceServiceImpl implements ServiceService {
  private final CsvService csvService;

  private final ServiceRepository serviceRepository;
  private final AppointmentRepository appointmentRepository;
  private final CsvBeanValidator csvBeanValidator;

  private final ModelMapper modelMapper;

  @Override
  @Transactional
  public void createServicesFromFile(@NonNull MultipartFile file) {
    List<Service> services =
        csvService.getElementsFromCsvFile(file, ServiceCsvBean.class).stream()
            .peek(csvBeanValidator::validateCsvBean)
            .map(this::csvBeanToEntity)
            .toList();

    serviceRepository.saveAll(services);
  }

  private Service csvBeanToEntity(@NonNull ServiceCsvBean csvBean) {
    Service service = modelMapper.map(csvBean, Service.class);

    appointmentRepository
        .findById(csvBean.getAppointmentId())
        .ifPresentOrElse(
            appointment -> appointment.addService(service),
            () -> {
              throw new AppointmentNotFoundException(csvBean.getAppointmentId());
            });

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
