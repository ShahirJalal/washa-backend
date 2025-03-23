package com.washa.backend.repository;

import com.washa.backend.model.Detergent;
import com.washa.backend.model.Dobi;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DetergentRepository extends JpaRepository<Detergent, Long> {
    List<Detergent> findByDobi(Dobi dobi);
}