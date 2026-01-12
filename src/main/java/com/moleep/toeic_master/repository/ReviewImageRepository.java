package com.moleep.toeic_master.repository;

import com.moleep.toeic_master.entity.ReviewImage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReviewImageRepository extends JpaRepository<ReviewImage, Long> {

    List<ReviewImage> findByReviewId(Long reviewId);

    @Query("SELECT ri FROM ReviewImage ri JOIN ri.review r WHERE r.user.id = :userId ORDER BY ri.createdAt DESC")
    Page<ReviewImage> findByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT COUNT(ri) FROM ReviewImage ri JOIN ri.review r WHERE r.user.id = :userId")
    long countByUserId(@Param("userId") Long userId);

    void deleteByReviewId(Long reviewId);
}
