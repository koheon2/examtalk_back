package com.moleep.toeic_master.config;

import com.moleep.toeic_master.entity.School;
import com.moleep.toeic_master.repository.SchoolRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataLoader implements CommandLineRunner {

    private final SchoolRepository schoolRepository;

    @Override
    @Transactional
    public void run(String... args) {
        loadSchools();
    }

    private void loadSchools() {
        if (schoolRepository.count() > 0) {
            log.info("Schools already exist, skipping initial data load");
            return;
        }

        List<School> schools = List.of(
                School.builder()
                        .name("갑천중")
                        .address("대전광역시 서구 월평동로 80")
                        .latitude(new BigDecimal("36.36143570"))
                        .longitude(new BigDecimal("127.36859460"))
                        .build(),
                School.builder()
                        .name("남선중")
                        .address("대전광역시 서구 월평북로 41 (월평동, 남선중학교)")
                        .latitude(new BigDecimal("36.36268670"))
                        .longitude(new BigDecimal("127.37169570"))
                        .build(),
                School.builder()
                        .name("둔산중")
                        .address("대전광역시 서구 갈마역로25번길 70")
                        .latitude(new BigDecimal("36.35343610"))
                        .longitude(new BigDecimal("127.37175820"))
                        .build(),
                School.builder()
                        .name("만년중")
                        .address("대전광역시 서구 만년남로 17")
                        .latitude(new BigDecimal("36.36660610"))
                        .longitude(new BigDecimal("127.37547130"))
                        .build(),
                School.builder()
                        .name("문정중")
                        .address("대전광역시 서구 둔산북로 232-11 (둔산동, 문정중학교)")
                        .latitude(new BigDecimal("36.35512930"))
                        .longitude(new BigDecimal("127.40053470"))
                        .build()
        );

        schoolRepository.saveAll(schools);
        log.info("Loaded {} schools", schools.size());
    }
}
