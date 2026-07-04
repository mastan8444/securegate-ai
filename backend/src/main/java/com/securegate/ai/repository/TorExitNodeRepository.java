package com.securegate.ai.repository;

import com.securegate.ai.entity.TorExitNode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TorExitNodeRepository extends JpaRepository<TorExitNode, Long> {
    Optional<TorExitNode> findByIpAddress(String ipAddress);
    boolean existsByIpAddress(String ipAddress);
}
