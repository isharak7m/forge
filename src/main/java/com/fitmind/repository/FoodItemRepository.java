package com.fitmind.repository;

import com.fitmind.entity.FoodItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FoodItemRepository extends JpaRepository<FoodItem, Long> {

    @Query("SELECT f FROM FoodItem f WHERE LOWER(f.name) LIKE LOWER(CONCAT('%', :query, '%')) ORDER BY f.name")
    List<FoodItem> searchByName(@Param("query") String query);

    @Query("SELECT f FROM FoodItem f WHERE LOWER(f.name) LIKE LOWER(CONCAT('%', :query, '%')) ORDER BY f.name")
    Page<FoodItem> searchByNamePaged(@Param("query") String query, Pageable pageable);

    long count();

    @Query("SELECT f.name FROM FoodItem f")
    List<String> findAllNames();
}
