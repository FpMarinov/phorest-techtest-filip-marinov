package com.phorest.service;

import com.phorest.exception.AppointmentNotFoundException;
import com.phorest.exception.PurchaseNotFoundException;
import com.phorest.model.csv.PurchaseCsvBean;
import com.phorest.model.entity.Appointment;
import com.phorest.model.entity.Purchase;
import com.phorest.model.request.PurchaseRequest;
import com.phorest.model.response.PurchaseResponse;
import com.phorest.repository.AppointmentRepository;
import com.phorest.repository.PurchaseRepository;
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
public class PurchaseServiceImpl implements PurchaseService {
  private static final int PURCHASE_FILE_PAGE_SIZE = 200;

  private final CsvService csvService;

  private final PurchaseRepository purchaseRepository;
  private final AppointmentRepository appointmentRepository;
  private final CsvBeanValidator csvBeanValidator;

  private final ModelMapper modelMapper;

  @Override
  @Transactional
  public void createPurchasesFromFile(@NonNull MultipartFile file) {
    int currentPageNumber = 0;
    Page<PurchaseCsvBean> currentPurchasePage;
    boolean isCurrentPageLast;

    do {
      currentPurchasePage =
          csvService.getElementsFromCsvFile(
              file, PurchaseCsvBean.class, currentPageNumber, PURCHASE_FILE_PAGE_SIZE);

      createPurchasesInPage(currentPurchasePage);

      isCurrentPageLast = currentPurchasePage.isLast();

      currentPageNumber++;
    } while (!isCurrentPageLast);
  }

  private void createPurchasesInPage(Page<PurchaseCsvBean> purchasePage) {
    List<PurchaseCsvBean> purchaseCsvBeans = purchasePage.getContent();

    Set<UUID> appointmentIds =
        purchaseCsvBeans.stream()
            .map(PurchaseCsvBean::getAppointmentId)
            .collect(Collectors.toSet());

    Map<UUID, Appointment> appointmentsById =
        appointmentRepository.findByIdIn(appointmentIds).stream()
            .collect(Collectors.toMap(Appointment::getId, Function.identity()));

    List<Purchase> purchases =
        purchaseCsvBeans.stream()
            .peek(csvBeanValidator::validateCsvBean)
            .map(csvBean -> csvBeanToEntity(csvBean, appointmentsById))
            .toList();

    purchaseRepository.saveAllAndFlush(purchases);
  }

  private Purchase csvBeanToEntity(
      @NonNull PurchaseCsvBean csvBean, @NonNull Map<UUID, Appointment> appointmentsById) {

    Purchase purchase = modelMapper.map(csvBean, Purchase.class);

    Appointment appointment = appointmentsById.get(csvBean.getAppointmentId());

    if (appointment == null) {
      throw new AppointmentNotFoundException(csvBean.getAppointmentId());
    }

    appointment.addPurchase(purchase);

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
