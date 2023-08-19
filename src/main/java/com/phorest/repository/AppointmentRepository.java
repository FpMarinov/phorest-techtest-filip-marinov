package com.phorest.repository;

import com.phorest.model.entity.Appointment;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {}
