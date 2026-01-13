# TOEIC Master

토익 시험 준비를 위한 스터디 매칭 및 고사장 리뷰 플랫폼 백엔드 API 서버

## 기술 스택

- **Framework**: Spring Boot 4.0.1
- **Language**: Java 21
- **Database**: PostgreSQL
- **Authentication**: JWT (JSON Web Token)
- **Storage**: AWS S3
- **Real-time**: WebSocket (STOMP)
- **Documentation**: Swagger/OpenAPI
- **Build Tool**: Gradle

## 주요 기능

### 1. 사용자 (User)
- 회원가입 / 로그인 (JWT 기반 인증)
- 프로필 조회 및 수정
- 프로필 이미지 업로드
- 성향(tendency) 기반 AI 임베딩 생성
- 포인트/스코어 시스템

### 2. 스터디 (Study)
- 스터디 생성 / 수정 / 삭제
- 스터디 목록 조회 및 검색 (시험 종류, 지역, 목표 점수 필터)
- 스터디 가입 신청 및 승인/거절
- AI 기반 스터디 추천 (사용자 성향 ↔ 스터디 콘텐츠 유사도)
- 내 스터디 조회

### 3. 고사장/학교 (School)
- 고사장 목록 조회 및 검색
- 위치 기반 주변 고사장 조회
- AI 기반 고사장 추천 (사용자 성향 ↔ 리뷰 기반 평가 유사도)
- AI 생성 고사장 평가 요약

### 4. 리뷰 (Review)
- 고사장 리뷰 작성 / 수정 / 삭제
- 리뷰 이미지 업로드 (최대 5장)
- 리뷰 좋아요 기능
- Predefined 태그 (추천, 시설 좋음, 조용함, 접근성 좋음)

### 5. 채팅 (Chat)
- WebSocket 기반 실시간 스터디 그룹 채팅
- 채팅 이미지 전송
- 채팅 기록 조회

## API 문서

서버 실행 후 Swagger UI에서 확인:
```
http://localhost:8080/swagger-ui.html
```

## 프로젝트 구조

```
src/main/java/com/moleep/toeic_master/
├── config/          # 설정 (Security, WebSocket, S3, Swagger)
├── controller/      # REST API 컨트롤러
├── dto/             # 요청/응답 DTO
│   ├── request/
│   └── response/
├── entity/          # JPA 엔티티
├── exception/       # 커스텀 예외
├── repository/      # JPA 리포지토리
├── security/        # JWT, UserDetails
└── service/         # 비즈니스 로직
```

## 환경 설정

### application.yml 설정 항목

```yaml
spring:
  datasource:
    url: jdbc:postgresql://<host>:<port>/<database>
    username: <username>
    password: <password>
  jpa:
    hibernate:
      ddl-auto: update

jwt:
  secret: <jwt-secret-key>
  expiration: 86400000  # 24시간

cloud:
  aws:
    s3:
      bucket: <bucket-name>
    credentials:
      access-key: <access-key>
      secret-key: <secret-key>
    region:
      static: ap-northeast-2
```

## 실행 방법

### 요구사항
- Java 21
- PostgreSQL
- AWS S3 버킷

### 빌드 및 실행

```bash
# 빌드
./gradlew build

# 실행
./gradlew bootRun

# 또는 JAR 실행
java -jar build/libs/toeic_master-0.0.1-SNAPSHOT.jar
```

## AI 추천 시스템

### 임베딩 기반 추천
- 외부 AI 서버 (`/embed`, `/venue/eval-embed`) 연동
- 사용자 성향 → 임베딩 벡터 변환
- 코사인 유사도 기반 스터디/고사장 추천
- 임베딩 캐시로 성능 최적화

### 비동기 처리
- 리뷰 등록 시 고사장 임베딩 업데이트는 비동기 처리
- 사용자 응답 지연 없이 AI 서버 호출

## 포인트 시스템

| 활동 | 포인트 |
|------|--------|
| 스터디 가입 승인 | +50 |
| 리뷰 작성 | +20 |

## 라이선스

MIT License
