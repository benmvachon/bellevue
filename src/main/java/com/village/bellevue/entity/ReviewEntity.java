package com.village.bellevue.entity;

import java.sql.Timestamp;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

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
@Table(name = "review")
public class ReviewEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "recipe", nullable = false)
    private SimpleRecipeEntity recipe;

    @ManyToOne
    @JoinColumn(name = "author", nullable = false)
    private ScrubbedUserEntity author;

    private ReviewRating review;

    private String content;

    private Timestamp created;
    private Timestamp updated;

    public enum ReviewRating {
        DISASTROUSLY, MUDDLINGLY, ADMIRABLY, SPLENDIDLY, JUBILANTLY;

        @JsonValue
        public String toValue() {
            return this.name().toLowerCase();
        }

        @JsonCreator
        public static ReviewRating fromString(String value) {
            return ReviewRating.valueOf(value.toUpperCase());
        }
    }
}
