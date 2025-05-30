package com.village.bellevue.entity;

import java.sql.Timestamp;
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
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
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
  private List<String> tags;

  @ManyToMany(fetch = FetchType.LAZY)
  @JoinTable(
    name = "forum_security",
    joinColumns = @JoinColumn(name = "forum"),
    inverseJoinColumns = @JoinColumn(name = "user")
  )
  private List<UserProfileEntity> users;

  public ForumEntity(ForumModel model) {
    this.id = model.getId();
    this.user = Objects.nonNull(model.getUser()) ? model.getUser().getId() : null;
    this.name = model.getName();
    this.created = model.getCreated();
    this.tags = model.getTags();
  }
}
