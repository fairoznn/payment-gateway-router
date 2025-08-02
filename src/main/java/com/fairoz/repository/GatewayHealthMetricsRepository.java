package com.fairoz.repository;

import com.fairoz.model.GatewayHealthMetrics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface GatewayHealthMetricsRepository extends JpaRepository<GatewayHealthMetrics, Long> {
    
    @Query("SELECT ghm FROM GatewayHealthMetrics ghm WHERE ghm.gatewayName = :gatewayName " +
           "AND ghm.windowStart >= :windowStart ORDER BY ghm.windowStart DESC")
    List<GatewayHealthMetrics> findByGatewayNameAndWindowStartAfter(
        @Param("gatewayName") String gatewayName, 
        @Param("windowStart") LocalDateTime windowStart);
    
    Optional<GatewayHealthMetrics> findTopByGatewayNameOrderByWindowStartDesc(String gatewayName);
    
    @Query("SELECT ghm FROM GatewayHealthMetrics ghm WHERE ghm.windowStart >= :windowStart")
    List<GatewayHealthMetrics> findAllByWindowStartAfter(@Param("windowStart") LocalDateTime windowStart);
}