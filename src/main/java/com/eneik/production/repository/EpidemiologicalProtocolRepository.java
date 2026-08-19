package com.eneik.production.repository;

import com.eneik.production.models.persistence.EpidemiologicalProtocolEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EpidemiologicalProtocolRepository extends JpaRepository<EpidemiologicalProtocolEntity, Long>, JpaSpecificationExecutor<EpidemiologicalProtocolEntity> {
    Optional<EpidemiologicalProtocolEntity> findByCode(String code);

    @Query("SELECT p FROM EpidemiologicalProtocolEntity p WHERE " +
           "LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.summary) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.code) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.category) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<EpidemiologicalProtocolEntity> searchByKeyword(@Param("keyword") String keyword);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE EpidemiologicalProtocolEntity p SET " +
           "p.code = COALESCE(:code, p.code), " +
           "p.title = COALESCE(:title, p.title), " +
           "p.category = COALESCE(:category, p.category), " +
           "p.version = COALESCE(:version, p.version), " +
           "p.status = COALESCE(:status, p.status), " +
           "p.summary = COALESCE(:summary, p.summary), " +
           "p.authorOrganization = COALESCE(:authorOrganization, p.authorOrganization), " +
           "p.publicationYear = COALESCE(:publicationYear, p.publicationYear), " +
           "p.recordType = COALESCE(:recordType, p.recordType) " +
           "WHERE p.id = :id AND p.status = :expectedStatus")
    int updateProtocolWithStatusGuard(
            @Param("id") Long id,
            @Param("expectedStatus") String expectedStatus,
            @Param("code") String code,
            @Param("title") String title,
            @Param("category") String category,
            @Param("version") String version,
            @Param("status") String status,
            @Param("summary") String summary,
            @Param("authorOrganization") String authorOrganization,
            @Param("publicationYear") Integer publicationYear,
            @Param("recordType") String recordType);
}
