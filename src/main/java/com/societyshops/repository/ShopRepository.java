package com.societyshops.repository;

import com.societyshops.entity.Shop;
import com.societyshops.enums.ShopStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShopRepository extends JpaRepository<Shop, Long> {
    List<Shop> findByIsApproved(Boolean isApproved);
    List<Shop> findByIsApprovedAndStatus(Boolean isApproved, ShopStatus status);
    List<Shop> findByIsApprovedAndCategory(Boolean isApproved, String category);
    List<Shop> findByOwnerId(Long ownerId);
    List<Shop> findByIsApprovedFalse();
}
