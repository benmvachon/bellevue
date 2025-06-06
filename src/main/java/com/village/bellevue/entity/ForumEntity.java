package com.village.bellevue.entity;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.village.bellevue.model.ForumModel;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
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

  @ElementCollection(fetch = FetchType.LAZY)
  @CollectionTable(
    name = "forum_tag",
    joinColumns = @JoinColumn(name = "forum")
  )
  @Column(name = "tag")
  private List<String> tags = new ArrayList<>();

  @ElementCollection(fetch = FetchType.LAZY)
  @CollectionTable(
    name = "forum_security",
    joinColumns = @JoinColumn(name = "forum")
  )
  @Column(name = "user")
  private List<Long> users = new ArrayList<>();

  public ForumEntity(ForumModel model) {
    this.id = model.getId();
    this.user = Objects.nonNull(model.getUser()) ? model.getUser().getId() : null;
    this.name = model.getName();
    this.created = model.getCreated();
    this.tags = model.getTags();
  }
}
