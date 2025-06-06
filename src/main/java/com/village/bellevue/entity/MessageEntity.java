package com.village.bellevue.entity;

import java.sql.Timestamp;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

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
@Table(name = "message")
public class MessageEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "sender")
  private UserProfileEntity sender;

  @ManyToOne
  @JoinColumn(name = "receiver")
  private UserProfileEntity receiver;

  @Column(columnDefinition = "TEXT")
  private String message;

  @Column(name = "`read`", nullable = false)
  private boolean read = false;

  private Timestamp created = new Timestamp(System.currentTimeMillis());
}
