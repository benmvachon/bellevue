package com.village.bellevue.entity;

import java.sql.Timestamp;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Table(name = "post")
public class PostEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "user")
  private UserProfileEntity user;

  @ManyToOne(cascade = CascadeType.MERGE)
  @JoinColumn(name = "parent", nullable = true)
  private PostEntity parent;

  @ManyToOne(cascade = CascadeType.MERGE)
  @JoinColumn(name = "forum")
  private ForumEntity forum;

  @Column(columnDefinition = "TEXT")
  private String content;

  private boolean deleted = false;

  private Timestamp created = new Timestamp(System.currentTimeMillis());
}
