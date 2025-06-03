package com.village.bellevue.entity;

import com.village.bellevue.entity.id.ForumSecurityId;
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
@Table(name = "forum_security")
@IdClass(ForumSecurityId.class)
public class ForumSecurityEntity {

  @Id private Long forum;

  @Id
  @ManyToOne
  @JoinColumn(name = "user", nullable = false)
  private UserProfileEntity user;
}
