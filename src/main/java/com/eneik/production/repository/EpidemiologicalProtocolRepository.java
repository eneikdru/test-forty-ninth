package com.eneik.production.repository;

import com.eneik.production.models.persistence.EpidemiologicalProtocolEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EpidemiologicalProtocolRepository extends JpaRepository<EpidemiologicalProtocolEntity, Long> {
    Optional<EpidemiologicalProtocolEntity> findByCode(String code);

    @Query("SELECT p FROM EpidemiologicalProtocolEntity p WHERE " +
           "LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.summary) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.code) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.category) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<EpidemiologicalProtocolEntity> searchByKeyword(@Param("keyword") String keyword);
}
