package com.backtoyou.repository;

import com.backtoyou.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Integer>, JpaSpecificationExecutor<Item> {
    List<Item> findByUserIdOrderByIdDesc(Integer userId);
    long countByType(Item.ItemType type);
    long countByStatus(Item.ItemStatus status);
    long countByUserId(Integer userId);
}
