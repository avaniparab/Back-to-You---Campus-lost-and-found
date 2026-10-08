package com.backtoyou.controller;

import com.backtoyou.dto.*;
import com.backtoyou.entity.Item;
import com.backtoyou.entity.User;
import com.backtoyou.repository.UserRepository;
import com.backtoyou.service.ItemService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/items")
public class ItemController {

    private final ItemService itemService;
    private final UserRepository userRepository;

    public ItemController(ItemService itemService, UserRepository userRepository) {
        this.itemService = itemService;
        this.userRepository = userRepository;
    }

    @PostMapping
    public ResponseEntity<ItemResponse> createItem(@RequestBody ItemCreateRequest request, Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ItemResponse.error("Unauthorized. Please log in to report an item."));
        }

        try {
            Item created = itemService.createItem(request, currentUser);
            return ResponseEntity.ok(ItemResponse.createSuccess("Item report submitted successfully", created.getId()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.ok(ItemResponse.error(e.getMessage()));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ItemResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.ok(ItemResponse.error("Failed to save item report due to a database error."));
        }
    }

    @GetMapping
    public ResponseEntity<ItemResponse> getItems(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status,
            @RequestParam(required = false, defaultValue = "false") boolean mine,
            @RequestParam(required = false) String search,
            Authentication authentication) {

        User currentUser = getCurrentUser(authentication);
        try {
            List<ItemDto> items = itemService.getItems(type, category, status, mine, search, currentUser);
            return ResponseEntity.ok(ItemResponse.listSuccess(items));
        } catch (Exception e) {
            return ResponseEntity.ok(ItemResponse.error("Failed to retrieve items."));
        }
    }

    @GetMapping("/my")
    public ResponseEntity<ItemResponse> getMyReports(Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ItemResponse.error("Unauthorized. Please log in."));
        }

        try {
            List<ItemDto> items = itemService.getMyReports(currentUser);
            return ResponseEntity.ok(ItemResponse.listSuccess(items));
        } catch (Exception e) {
            return ResponseEntity.ok(ItemResponse.error("Failed to retrieve items."));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ItemResponse> getItemById(@PathVariable Integer id) {
        try {
            ItemDto item = itemService.getItemById(id);
            return ResponseEntity.ok(ItemResponse.singleSuccess(item));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.ok(ItemResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.ok(ItemResponse.error("Database error while fetching item details."));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ItemResponse> updateItem(
            @PathVariable Integer id,
            @RequestBody ItemUpdateRequest request,
            Authentication authentication) {

        User currentUser = getCurrentUser(authentication);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ItemResponse.error("Unauthorized. Please log in."));
        }

        try {
            itemService.updateItem(id, request, currentUser);
            return ResponseEntity.ok(ItemResponse.successMessage("Item updated successfully."));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ItemResponse.error(e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.ok(ItemResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.ok(ItemResponse.error("Failed to update item due to a database error."));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ItemResponse> deleteItem(@PathVariable Integer id, Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ItemResponse.error("Unauthorized. Please log in."));
        }

        try {
            itemService.deleteItem(id, currentUser);
            return ResponseEntity.ok(ItemResponse.successMessage("Item deleted successfully."));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ItemResponse.error(e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.ok(ItemResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.ok(ItemResponse.error("Failed to delete item due to a database error."));
        }
    }

    private User getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return null;
        }
        return userRepository.findByEmail(authentication.getName()).orElse(null);
    }
}
