package com.washa.backend.repository;

import com.washa.backend.model.Dobi;
import com.washa.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DobiRepository extends JpaRepository<Dobi, Long> {
    List<Dobi> findByOwner(User owner);
    List<Dobi> findByIsActiveTrue();
}