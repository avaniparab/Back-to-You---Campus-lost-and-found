package com.backtoyou.service;

import com.backtoyou.dto.ItemCreateRequest;
import com.backtoyou.dto.ItemDto;
import com.backtoyou.dto.ItemUpdateRequest;
import com.backtoyou.entity.Item;
import com.backtoyou.entity.User;
import com.backtoyou.repository.ItemRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ItemService {

    private static final List<String> ALLOWED_CATEGORIES = List.of(
        "Electronics", "Documents", "Accessories", "Books", "Clothing", "Stationery", "Others"
    );

    private final ItemRepository itemRepository;

    public ItemService(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    public Item createItem(ItemCreateRequest request, User currentUser) {
        if (currentUser == null) {
            throw new SecurityException("Unauthorized. Please log in to report an item.");
        }

        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Item title/name is required.");
        }

        String matchedCategory = normalizeCategory(request.getCategory());
        if (matchedCategory == null) {
            throw new IllegalArgumentException("Invalid or missing category.");
        }

        String typeStr = request.getType() != null ? request.getType().trim().toUpperCase() : "LOST";
        Item.ItemType itemType;
        try {
            itemType = Item.ItemType.valueOf(typeStr);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid item report type. Must be LOST or FOUND.");
        }

        if (request.getLocation() == null || request.getLocation().trim().isEmpty()) {
            throw new IllegalArgumentException("Location is required.");
        }

        if (request.getDate() == null || request.getDate().trim().isEmpty()) {
            throw new IllegalArgumentException("Date is required.");
        }

        LocalDate parsedDate;
        try {
            parsedDate = LocalDate.parse(request.getDate().trim());
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date format. Expected YYYY-MM-DD.");
        }

        if (request.getDescription() == null || request.getDescription().trim().isEmpty()) {
            throw new IllegalArgumentException("Description is required.");
        }

        Item item = new Item();
        item.setUser(currentUser);
        item.setTitle(request.getTitle().trim());
        item.setDescription(request.getDescription().trim());
        item.setCategory(matchedCategory);
        item.setLocation(request.getLocation().trim());
        item.setDate(parsedDate);
        item.setType(itemType);
        item.setImage(request.getImage() != null && !request.getImage().trim().isEmpty() ? request.getImage().trim() : null);
        item.setStatus(Item.ItemStatus.ACTIVE);

        return itemRepository.save(item);
    }

    @Transactional(readOnly = true)
    public List<ItemDto> getItems(String type, String category, String status, boolean mine, String search, User currentUser) {
        if (mine && currentUser == null) {
            return List.of();
        }

        Specification<Item> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (type != null && !type.trim().isEmpty()) {
                try {
                    Item.ItemType itemType = Item.ItemType.valueOf(type.trim().toUpperCase());
                    predicates.add(cb.equal(root.get("type"), itemType));
                } catch (IllegalArgumentException ignored) {
                }
            }

            if (category != null && !category.trim().isEmpty()) {
                predicates.add(cb.equal(cb.lower(root.get("category")), category.trim().toLowerCase()));
            }

            if (status != null && !status.trim().isEmpty()) {
                try {
                    Item.ItemStatus itemStatus = Item.ItemStatus.valueOf(status.trim().toUpperCase());
                    predicates.add(cb.equal(root.get("status"), itemStatus));
                } catch (IllegalArgumentException ignored) {
                }
            }

            if (mine && currentUser != null) {
                predicates.add(cb.equal(root.get("user").get("id"), currentUser.getId()));
            }

            if (search != null && !search.trim().isEmpty()) {
                String pattern = "%" + search.trim().toLowerCase() + "%";
                Predicate titleMatch = cb.like(cb.lower(root.get("title")), pattern);
                Predicate descMatch = cb.like(cb.lower(root.get("description")), pattern);
                Predicate locMatch = cb.like(cb.lower(root.get("location")), pattern);
                Predicate catMatch = cb.like(cb.lower(root.get("category")), pattern);
                predicates.add(cb.or(titleMatch, descMatch, locMatch, catMatch));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return itemRepository.findAll(spec, Sort.by(Sort.Direction.DESC, "id"))
            .stream()
            .map(item -> ItemDto.fromEntity(item, false))
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ItemDto getItemById(Integer id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid item ID.");
        }

        Item item = itemRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Item not found"));

        return ItemDto.fromEntity(item, true);
    }

    @Transactional(readOnly = true)
    public List<ItemDto> getMyReports(User currentUser) {
        if (currentUser == null) {
            throw new SecurityException("Unauthorized. Please log in.");
        }

        return itemRepository.findByUserIdOrderByIdDesc(currentUser.getId())
            .stream()
            .map(item -> ItemDto.fromEntity(item, false))
            .collect(Collectors.toList());
    }

    public Item updateItem(Integer id, ItemUpdateRequest request, User currentUser) {
        if (currentUser == null) {
            throw new SecurityException("Unauthorized. Please log in.");
        }

        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid or missing item ID.");
        }

        Item item = itemRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Item not found."));

        boolean isOwner = item.getUser() != null && item.getUser().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getRole() == User.Role.ADMIN;

        if (!isOwner && !isAdmin) {
            throw new SecurityException("Forbidden. You do not have permission to edit this item.");
        }

        if (request.getTitle() != null && !request.getTitle().trim().isEmpty()) {
            item.setTitle(request.getTitle().trim());
        }

        if (request.getDescription() != null && !request.getDescription().trim().isEmpty()) {
            item.setDescription(request.getDescription().trim());
        }

        if (request.getCategory() != null && !request.getCategory().trim().isEmpty()) {
            String matchedCat = normalizeCategory(request.getCategory());
            if (matchedCat != null) {
                item.setCategory(matchedCat);
            }
        }

        if (request.getLocation() != null && !request.getLocation().trim().isEmpty()) {
            item.setLocation(request.getLocation().trim());
        }

        if (request.getDate() != null && !request.getDate().trim().isEmpty()) {
            try {
                item.setDate(LocalDate.parse(request.getDate().trim()));
            } catch (DateTimeParseException ignored) {
            }
        }

        if (request.getStatus() != null && !request.getStatus().trim().isEmpty()) {
            try {
                Item.ItemStatus newStatus = Item.ItemStatus.valueOf(request.getStatus().trim().toUpperCase());
                item.setStatus(newStatus);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid status value. Must be ACTIVE or RESOLVED.");
            }
        }

        return itemRepository.save(item);
    }

    public void deleteItem(Integer id, User currentUser) {
        if (currentUser == null) {
            throw new SecurityException("Unauthorized. Please log in.");
        }

        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid item ID.");
        }

        Item item = itemRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Item not found."));

        boolean isOwner = item.getUser() != null && item.getUser().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getRole() == User.Role.ADMIN;

        if (!isOwner && !isAdmin) {
            throw new SecurityException("Forbidden. You can only delete your own item reports.");
        }

        itemRepository.delete(item);
    }

    private String normalizeCategory(String category) {
        if (category == null || category.trim().isEmpty()) {
            return null;
        }
        for (String allowed : ALLOWED_CATEGORIES) {
            if (allowed.equalsIgnoreCase(category.trim())) {
                return allowed;
            }
        }
        return null;
    }
}
