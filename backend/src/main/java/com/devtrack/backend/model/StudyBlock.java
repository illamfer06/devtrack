package com.devtrack.backend.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "study_blocks")
public class StudyBlock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private boolean active;
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public StudyBlock(){}

    public StudyBlock(
            String title,
            boolean active
    ) {
        this.title = title;
        this.active = active;
    }

    public StudyBlock(
            Long id,
            String title,
            boolean active
    ) {
        this.id = id;
        this.title = title;
        this.active = active;
    }

    public Long getId() { return id;}
    public String getTitle() { return title;}
    public boolean isActive() { return active;}
    public LocalDateTime getCreatedAt() { return createdAt;}
    public LocalDateTime getUpdatedAt() { return updatedAt;}

    public void setTitle(String title) { this.title = title;}
    public void setActive(boolean active) { this.active = active;}

}
