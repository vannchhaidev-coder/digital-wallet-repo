package com.vannchhai.authservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import lombok.Data;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;

import java.time.LocalDateTime;

@Data
@MappedSuperclass
@EntityListeners(AuditableEntity.class)
public class AuditableEntity {

  @Column(name = "created_at", updatable = false, nullable = false)
  private LocalDateTime createdAt;

  @CreatedBy
  @Column(name = "created_by", updatable = false, nullable = false, length = 50)
  private String createdBy;

  @Column(name = "updated_at", updatable = false, nullable = false)
  private LocalDateTime updatedAt;

  @LastModifiedBy
  @Column(name = "updated_by", updatable = false, nullable = false, length = 50)
  private String updatedBy;

  @PrePersist
  protected void onCreate() {
    this.createdAt = LocalDateTime.now();
  }

  @PrePersist
  protected void onUpdate() {
    this.updatedAt = LocalDateTime.now();
  }
}
