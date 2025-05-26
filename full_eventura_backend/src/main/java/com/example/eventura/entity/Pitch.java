package com.example.eventura.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "Pitches"
//        ,
//        uniqueConstraints = {
//                @UniqueConstraint(columnNames = {"provider_id", "request_id"})
//        }
)
@Data
public class Pitch {

    public enum Status {
        PENDING,
        WIN,
        LOSE
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "request_id", nullable = false)
    private ServiceRequest request;

    @ManyToOne
    @JoinColumn(name = "provider_id", nullable = false)
    private User provider;

    @Column(name = "pitch_details")
    private String message;

    @Column(name = "proposed_price")
    private Double proposedPrice;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private Status status = Status.PENDING;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    public void setUpdatedAt() {
        this.updatedAt = LocalDateTime.now();
    }
}