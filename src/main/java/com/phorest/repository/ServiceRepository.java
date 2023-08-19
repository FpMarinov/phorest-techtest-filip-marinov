package com.phorest.repository;

import com.phorest.model.entity.Service;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceRepository extends JpaRepository<Service, UUID> {}
