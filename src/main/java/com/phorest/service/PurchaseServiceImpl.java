package com.phorest.service;

import com.phorest.exception.AppointmentNotFoundException;
import com.phorest.exception.PurchaseNotFoundException;
import com.phorest.model.csv.PurchaseCsvBean;
import com.phorest.model.entity.Purchase;
import com.phorest.model.request.PurchaseRequest;
import com.phorest.model.response.PurchaseResponse;
import com.phorest.repository.AppointmentRepository;
import com.phorest.repository.PurchaseRepository;
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
public class PurchaseServiceImpl implements PurchaseService {
  private final CsvService csvService;

  private final PurchaseRepository purchaseRepository;
  private final AppointmentRepository appointmentRepository;
  private final CsvBeanValidator csvBeanValidator;

  private final ModelMapper modelMapper;

  @Override
  @Transactional
  public void createPurchasesFromFile(@NonNull MultipartFile file) {
    List<Purchase> purchases =
        csvService.getElementsFromCsvFile(file, PurchaseCsvBean.class).stream()
            .peek(csvBeanValidator::validateCsvBean)
            .map(this::csvBeanToEntity)
            .toList();

    purchaseRepository.saveAll(purchases);
  }

  private Purchase csvBeanToEntity(@NonNull PurchaseCsvBean csvBean) {
    Purchase purchase = modelMapper.map(csvBean, Purchase.class);

    appointmentRepository
        .findById(csvBean.getAppointmentId())
        .ifPresentOrElse(
            appointment -> appointment.addPurchase(purchase),
            () -> {
              throw new AppointmentNotFoundException(csvBean.getAppointmentId());
            });

    return purchase;
  }

  @Override
  @Transactional
  public PurchaseResponse updatePurchase(
      @NonNull UUID purchaseId, @NonNull PurchaseRequest purchaseRequest) {
    Purchase purchase = getPurchase(purchaseId);

    modelMapper.map(purchaseRequest, purchase);

    return modelMapper.map(purchase, PurchaseResponse.class);
  }

  @Override
  @Transactional
  public void deletePurchase(@NonNull UUID purchaseId) {
    Purchase purchase = getPurchase(purchaseId);

    purchaseRepository.delete(purchase);
  }

  @Override
  @Transactional(readOnly = true)
  public PurchaseResponse getPurchaseResponse(@NonNull UUID purchaseId) {
    Purchase purchase = getPurchase(purchaseId);

    return modelMapper.map(purchase, PurchaseResponse.class);
  }

  private Purchase getPurchase(@NonNull UUID purchaseId) {
    return purchaseRepository
        .findById(purchaseId)
        .orElseThrow(() -> new PurchaseNotFoundException(purchaseId));
  }
}
