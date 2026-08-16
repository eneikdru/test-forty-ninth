package com.eneik.production.repository;

import com.eneik.production.models.persistence.InternalTaskEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface InternalTaskRepository extends JpaRepository<InternalTaskEntity, String> {

    @Modifying
    @Query("UPDATE InternalTaskEntity t SET t.status = :newStatus, t.githubPrState = :githubPrState, t.updatedAt = :updatedAt WHERE t.id = :id AND t.status = :expectedStatus")
    int updateStatusAtomically(
            @Param("id") String id,
            @Param("expectedStatus") String expectedStatus,
            @Param("newStatus") String newStatus,
            @Param("githubPrState") String githubPrState,
            @Param("updatedAt") LocalDateTime updatedAt
    );
}
