# 📸 PATATA_Server

**숨은 사진 스팟을 지도에서 발견하고 공유하는 위치 기반 커뮤니티 서비스**  
이 저장소는 `PATATA` 서비스의 백엔드 서버 코드입니다.

---

## 🧭 서비스 소개

> **PATATA**는 사용자가 직접 발견한 포토 스팟을 지도 위에 공유하고,  
> 다른 사용자들과 함께 숨겨진 장소를 발견할 수 있는 위치 기반 서비스입니다.

---

## 📱 앱 다운로드

- [App Store에서 PATATA 다운로드하기](https://apps.apple.com/app/id6742177268)

---

## 🌐 배포 주소

- **서버 URL**
  - http://patata.kr:8080  
  - https://patata.kr
- **API 문서 (Swagger)**
  - [https://patata.kr/swagger-ui/index.html#/](https://patata.kr/swagger-ui/index.html#/)

---

## 🛠 사용 기술 스택

| 분류       | 기술 |
|------------|------|
| Language   | Java 17 |
| Framework  | Spring Boot 3.x |
| DB         | MySQL (RDS) |
| Auth       | OAuth2 (Google, Apple), JWT |
| Infra      | AWS EC2, S3, Lambda |
| CI/CD      | GitHub Actions + Docker |
| 기타       | JTS GeometryFactory, ST_Distance_Sphere |

---

## ✨ 주요 기능 및 설명

### ✅ 구글/애플 로그인 & JWT 인증

- Spring Security + OAuth2를 이용하여 **Google/Apple 로그인**을 구현하였고, 로그인 후에는 자체 **JWT 토큰을 발급**합니다.
- 클라이언트는 발급받은 JWT를 헤더에 포함하여 인증된 요청을 보낼 수 있으며, 토큰 만료 시 **Refresh Token을 이용한 재발급**이 가능합니다.
- **탈퇴 시** 해당 계정의 리프레시 토큰을 무효화하고, 소셜 계정 연동도 해제합니다.

### ✅ 스팟 이미지 등록 (S3 + Lambda 리사이징)

- 사용자가 업로드한 이미지는 AWS S3 버킷에 저장됩니다.
- S3에 업로드될 때 **S3 이벤트 트리거**가 동작하여 **AWS Lambda 함수**가 실행되고,
  - 원본 이미지를 기반으로 400/800/1200px 리사이징된 이미지를 생성해 다시 S3에 저장합니다.
- DB에는 각각의 이미지 URL이 함께 저장되어 클라이언트에서 다양한 해상도로 불러올 수 있습니다.

### ✅ 지도 기반 스팟 검색

- 사용자의 현재 위치를 기준으로 특정 반경 내의 스팟을 조회할 수 있습니다.
- 이를 위해 `JTS GeometryFactory`를 사용하여 DB에 좌표를 `Point` 타입으로 저장하고,
- MySQL의 공간 함수인 `ST_Distance_Sphere()`를 이용해 **반경 거리 계산 및 검색**을 구현했습니다.
- 이 기능은 **지도에 보이는 영역 내에서만 스팟을 불러오기 때문에** 성능 및 사용자 경험을 모두 고려한 핵심 기능입니다.

### ✅ 그 외 주요 기능

- 키워드 기반 스팟 검색
- 오늘의 추천 스팟 노출
- 리뷰 작성 및 삭제
- 사용자/스팟/리뷰 신고 기능
- 프로필 이미지 업로드 및 닉네임 변경

---

## 📂 프로젝트 디렉토리 구조

```bash
src/
├── main/
│   ├── java/
│   │   └── PATATA/
│   │       ├── auth/
│   │       │   ├── jwt/
│   │       │   └── oauth/
│   │       ├── domain/
│   │       │   ├── member/
│   │       │   ├── report/
│   │       │   └── spot/
│   │       ├── global/
│   │       ├── infra/
│   │       └── PatataApplication.java
│   └── resources/
│       └── application.yml
