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
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    public StudyBlock(){}

    public StudyBlock(
            String title,
            boolean active,
            AppUser user
    ) {
        this.title = title;
        this.active = active;
        this.user = user;
    }

    public StudyBlock(
            Long id,
            String title,
            boolean active,
            AppUser user
    ) {
        this.id = id;
        this.title = title;
        this.active = active;
        this.user = user;
    }

    public Long getId() { return id;}
    public String getTitle() { return title;}
    public boolean isActive() { return active;}
    public LocalDateTime getCreatedAt() { return createdAt;}
    public LocalDateTime getUpdatedAt() { return updatedAt;}
    public AppUser getUser() { return user;}

    public void setTitle(String title) { this.title = title;}
    public void setActive(boolean active) { this.active = active;}
}
