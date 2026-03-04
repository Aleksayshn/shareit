package com.ct5121.shareit.item.repository;

import com.ct5121.shareit.item.model.Item;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {
    @EntityGraph(attributePaths = {"owner"})
    List<Item> findByOwnerIdOrderByIdAsc(Long ownerId);

    @Query("""
            SELECT i
            FROM Item i
            WHERE i.available = true
              AND (
                LOWER(i.name) LIKE LOWER(CONCAT('%', :text, '%'))
                OR LOWER(i.description) LIKE LOWER(CONCAT('%', :text, '%'))
              )
            """)
    List<Item> searchItems(String text, Pageable pageable);

    List<Item> findByRequestId(Long requestId);
}
