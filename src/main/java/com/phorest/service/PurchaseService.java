package com.phorest.service;

import com.phorest.model.request.PurchaseRequest;
import com.phorest.model.response.PurchaseResponse;
import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;

public interface PurchaseService {
  void createPurchasesFromFile(MultipartFile file);

  PurchaseResponse updatePurchase(UUID purchaseId, PurchaseRequest purchaseRequest);

  void deletePurchase(UUID purchaseId);

  PurchaseResponse getPurchaseResponse(UUID purchaseId);
}
