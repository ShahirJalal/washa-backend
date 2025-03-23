package com.washa.backend.repository;

import com.washa.backend.model.Rider;
import com.washa.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface RiderRepository extends JpaRepository<Rider, Long> {
    Optional<Rider> findByUser(User user);
    List<Rider> findByIsAvailableTrue();
}