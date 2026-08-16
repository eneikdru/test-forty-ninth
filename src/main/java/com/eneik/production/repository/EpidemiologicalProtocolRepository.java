package com.eneik.production.repository;

import com.eneik.production.models.persistence.EpidemiologicalProtocolEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EpidemiologicalProtocolRepository extends JpaRepository<EpidemiologicalProtocolEntity, Long> {
    Optional<EpidemiologicalProtocolEntity> findByCode(String code);
}
