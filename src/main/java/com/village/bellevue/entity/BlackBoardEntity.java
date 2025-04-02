package com.village.bellevue.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "blackboard")
public class BlackBoardEntity {
  @Id
  @JoinColumn(name = "user", nullable = false)
  private Long user;

  @Column private String blackboard;
}
