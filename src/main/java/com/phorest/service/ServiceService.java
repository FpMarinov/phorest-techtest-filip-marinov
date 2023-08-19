package com.phorest.service;

import com.phorest.model.request.ServiceRequest;
import com.phorest.model.response.ServiceResponse;
import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;

public interface ServiceService {
  void createServicesFromFile(MultipartFile file);

  ServiceResponse updateService(UUID serviceId, ServiceRequest serviceRequest);

  void deleteService(UUID serviceId);

  ServiceResponse getServiceResponse(UUID serviceId);
}
