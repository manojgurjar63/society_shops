package com.societyshops.service;

import com.societyshops.dto.ShopRequest;
import com.societyshops.entity.Shop;
import com.societyshops.entity.User;
import com.societyshops.enums.ShopStatus;
import com.societyshops.repository.ShopRepository;
import com.societyshops.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ShopService {

    private final ShopRepository shopRepository;
    private final UserRepository userRepository;

    public Shop registerShop(ShopRequest request, Long ownerId) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Shop shop = Shop.builder()
                .owner(owner)
                .name(request.getName())
                .description(request.getDescription())
                .category(request.getCategory())
                .phone(request.getPhone())
                .address(request.getAddress())
                .openTime(request.getOpenTime())
                .closeTime(request.getCloseTime())
                .status(ShopStatus.CLOSED)
                .isApproved(false)
                .build();

        return shopRepository.save(shop);
    }

    public Shop toggleStatus(Long shopId, Long ownerId) {
        Shop shop = getShopOwnedBy(shopId, ownerId);
        shop.setStatus(shop.getStatus() == ShopStatus.OPEN ? ShopStatus.CLOSED : ShopStatus.OPEN);
        return shopRepository.save(shop);
    }

    public Shop updateShop(Long shopId, ShopRequest request, Long ownerId) {
        Shop shop = getShopOwnedBy(shopId, ownerId);
        shop.setName(request.getName());
        shop.setDescription(request.getDescription());
        shop.setCategory(request.getCategory());
        shop.setPhone(request.getPhone());
        shop.setAddress(request.getAddress());
        shop.setOpenTime(request.getOpenTime());
        shop.setCloseTime(request.getCloseTime());
        return shopRepository.save(shop);
    }

    public Shop approveShop(Long shopId) {
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new RuntimeException("Shop not found"));
        shop.setIsApproved(true);
        return shopRepository.save(shop);
    }

    public void rejectShop(Long shopId) {
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new RuntimeException("Shop not found"));
        shopRepository.delete(shop);
    }

    public List<Shop> getApprovedShops() {
        return shopRepository.findByIsApproved(true);
    }

    public List<Shop> getApprovedShopsByStatus(ShopStatus status) {
        return shopRepository.findByIsApprovedAndStatus(true, status);
    }

    public List<Shop> getMyShops(Long ownerId) {
        return shopRepository.findByOwnerId(ownerId);
    }

    public List<Shop> getPendingShops() {
        return shopRepository.findByIsApprovedFalse();
    }

    private Shop getShopOwnedBy(Long shopId, Long ownerId) {
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new RuntimeException("Shop not found"));
        if (!shop.getOwner().getId().equals(ownerId)) {
            throw new RuntimeException("You do not own this shop");
        }
        return shop;
    }
}
