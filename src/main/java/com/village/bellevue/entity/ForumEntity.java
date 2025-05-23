package com.village.bellevue.entity;

import java.sql.Timestamp;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.village.bellevue.model.ForumModel;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Table(name = "forum")
public class ForumEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private Long user;

  @Column(nullable = false, unique = true)
  private String name;

  private String description;

  private Timestamp created = new Timestamp(System.currentTimeMillis());

  public ForumEntity(ForumModel model) {
    this.id = model.getId();
    this.user = Objects.nonNull(model.getUser()) ? model.getUser().getId() : null;
    this.name = model.getName();
    this.created = model.getCreated();
  }
}
