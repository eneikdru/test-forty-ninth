package com.eneik.production.repository;

import com.eneik.production.models.persistence.MaterialEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MaterialRepository extends JpaRepository<MaterialEntity, Long> {

    @Query("SELECT m FROM MaterialEntity m WHERE " +
           "(:query IS NULL OR :query = '' OR " +
           "LOWER(m.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(m.description) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(m.content) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<MaterialEntity> searchMaterials(@Param("query") String query, Pageable pageable);
}
