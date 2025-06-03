package com.village.bellevue.entity;

import com.village.bellevue.entity.id.ForumTagId;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
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
@Table(name = "forum_tag")
@IdClass(ForumTagId.class)
public class ForumTagEntity {

  @Id private String tag;

  @Id
  @ManyToOne
  @JoinColumn(name = "forum", nullable = false)
  private ForumEntity forum;
}
