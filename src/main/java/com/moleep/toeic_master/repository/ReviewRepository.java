package com.moleep.toeic_master.repository;

import com.moleep.toeic_master.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    Page<Review> findBySchoolId(Long schoolId, Pageable pageable);
    boolean existsByUserIdAndSchoolId(Long userId, Long schoolId);
}
