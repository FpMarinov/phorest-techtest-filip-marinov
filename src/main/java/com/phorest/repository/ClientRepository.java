package com.phorest.repository;

import com.phorest.model.entity.Client;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClientRepository extends JpaRepository<Client, UUID> {
  List<Client> findByBannedFalse();

  List<Client> findByIdIn(Collection<UUID> ids);
}
