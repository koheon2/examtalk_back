package com.moleep.toeic_master.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "schools")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class School {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false, length = 500)
    private String address;

    @Column(nullable = false, precision = 10, scale = 8)
    private BigDecimal latitude;

    @Column(nullable = false, precision = 11, scale = 8)
    private BigDecimal longitude;

    @Column(name = "avg_rating", precision = 2, scale = 1)
    @Builder.Default
    private BigDecimal avgRating = BigDecimal.ZERO;

    @Column(columnDefinition = "TEXT")
    private String evaluation;

    @Column(columnDefinition = "BYTEA")
    private byte[] embedding;

    @OneToMany(mappedBy = "school", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Review> reviews = new ArrayList<>();

    public void updateAvgRating() {
        if (reviews.isEmpty()) {
            this.avgRating = BigDecimal.ZERO;
        } else {
            double avg = reviews.stream()
                    .mapToInt(Review::getRating)
                    .average()
                    .orElse(0.0);
            this.avgRating = BigDecimal.valueOf(avg).setScale(1, java.math.RoundingMode.HALF_UP);
        }
    }
}
