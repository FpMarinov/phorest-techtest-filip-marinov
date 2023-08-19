package com.phorest.repository;

import com.phorest.model.entity.Purchase;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PurchaseRepository extends JpaRepository<Purchase, UUID> {}
