package com.washa.backend.repository;

import com.washa.backend.model.Dobi;
import com.washa.backend.model.DobiService;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DobiServiceRepository extends JpaRepository<DobiService, Long> {
    List<DobiService> findByDobi(Dobi dobi);
}