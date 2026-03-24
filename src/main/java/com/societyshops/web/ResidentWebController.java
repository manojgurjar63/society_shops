package com.societyshops.web;

import com.societyshops.entity.User;
import com.societyshops.repository.UserRepository;
import com.societyshops.service.FavoriteService;
import com.societyshops.service.InventoryService;
import com.societyshops.service.ShopService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/web/resident")
@RequiredArgsConstructor
public class ResidentWebController {

    private final ShopService shopService;
    private final InventoryService inventoryService;
    private final FavoriteService favoriteService;
    private final UserRepository userRepository;

    @GetMapping("/dashboard")
    public String dashboard(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        var shops = shopService.getApprovedShops();
        var favorites = favoriteService.getMyFavorites(getUserId(userDetails));
        model.addAttribute("shops", shops);
        model.addAttribute("favorites", favorites);
        model.addAttribute("openCount", shops.stream().filter(s -> s.getStatus().name().equals("OPEN")).count());
        return "resident/dashboard";
    }

    @GetMapping("/shops/{shopId}/inventory")
    public String inventory(@PathVariable Long shopId, Model model) {
        model.addAttribute("items", inventoryService.getAvailableItems(shopId));
        model.addAttribute("shopId", shopId);
        return "resident/inventory";
    }

    @PostMapping("/favorites/{shopId}/add")
    public String addFavorite(@PathVariable Long shopId,
                              @AuthenticationPrincipal UserDetails userDetails,
                              RedirectAttributes ra) {
        try {
            favoriteService.addFavorite(getUserId(userDetails), shopId);
            ra.addFlashAttribute("success", "Added to favorites!");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/web/resident/dashboard";
    }

    @PostMapping("/favorites/{shopId}/remove")
    public String removeFavorite(@PathVariable Long shopId,
                                 @AuthenticationPrincipal UserDetails userDetails,
                                 RedirectAttributes ra) {
        favoriteService.removeFavorite(getUserId(userDetails), shopId);
        ra.addFlashAttribute("success", "Removed from favorites.");
        return "redirect:/web/resident/dashboard";
    }

    private Long getUserId(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getId();
    }
}
