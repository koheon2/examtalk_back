package com.moleep.toeic_master.repository;

import com.moleep.toeic_master.entity.ReviewLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReviewLikeRepository extends JpaRepository<ReviewLike, Long> {
    Optional<ReviewLike> findByReviewIdAndUserId(Long reviewId, Long userId);
    boolean existsByReviewIdAndUserId(Long reviewId, Long userId);
    long countByReviewId(Long reviewId);
    void deleteByReviewIdAndUserId(Long reviewId, Long userId);
}
