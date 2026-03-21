package com.societyshops.controller;

import com.societyshops.dto.ApiResponse;
import com.societyshops.dto.ShopRequest;
import com.societyshops.entity.Shop;
import com.societyshops.enums.ShopStatus;
import com.societyshops.repository.UserRepository;
import com.societyshops.service.ShopService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shops")
@RequiredArgsConstructor
public class ShopController {

    private final ShopService shopService;
    private final UserRepository userRepository;

    @GetMapping
    @PreAuthorize("hasRole('RESIDENT')")
    public ResponseEntity<ApiResponse<List<Shop>>> getApprovedShops() {
        return ResponseEntity.ok(ApiResponse.success("Shops fetched", shopService.getApprovedShops()));
    }

    @GetMapping("/filter")
    @PreAuthorize("hasRole('RESIDENT')")
    public ResponseEntity<ApiResponse<List<Shop>>> filterByStatus(@RequestParam ShopStatus status) {
        return ResponseEntity.ok(ApiResponse.success("Shops fetched", shopService.getApprovedShopsByStatus(status)));
    }

    @PostMapping
    @PreAuthorize("hasRole('SHOPKEEPER')")
    public ResponseEntity<ApiResponse<Shop>> registerShop(
            @Valid @RequestBody ShopRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Shop shop = shopService.registerShop(request, getUserId(userDetails));
        return ResponseEntity.ok(ApiResponse.success("Shop registered, awaiting approval", shop));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SHOPKEEPER')")
    public ResponseEntity<ApiResponse<Shop>> updateShop(
            @PathVariable Long id,
            @Valid @RequestBody ShopRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success("Shop updated", shopService.updateShop(id, request, getUserId(userDetails))));
    }

    @PutMapping("/{id}/toggle")
    @PreAuthorize("hasRole('SHOPKEEPER')")
    public ResponseEntity<ApiResponse<Shop>> toggleStatus(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success("Status updated", shopService.toggleStatus(id, getUserId(userDetails))));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('SHOPKEEPER')")
    public ResponseEntity<ApiResponse<List<Shop>>> getMyShops(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success("Your shops", shopService.getMyShops(getUserId(userDetails))));
    }

    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<Shop>>> getPending() {
        return ResponseEntity.ok(ApiResponse.success("Pending shops", shopService.getPendingShops()));
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Shop>> approve(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Shop approved", shopService.approveShop(id)));
    }

    @DeleteMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> reject(@PathVariable Long id) {
        shopService.rejectShop(id);
        return ResponseEntity.ok(ApiResponse.success("Shop rejected", null));
    }

    private Long getUserId(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getId();
    }
}
