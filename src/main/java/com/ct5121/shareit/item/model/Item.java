package com.ct5121.shareit.item.model;

import com.ct5121.shareit.user.model.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "items")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private String description;
    @Column(nullable = false)
    private boolean available; // rental availability status
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner; // owner of item
    @Column(name = "request_id")
    private Long requestId;
}
