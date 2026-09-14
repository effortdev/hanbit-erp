# 한빛전자 ERP 배포용 이미지 — WAR 빌드 + Tomcat 9(Servlet 4.0) 런타임.
#
# 빌드 (반드시 프로젝트 루트에서, 컨텍스트를 루트로 지정):
#   docker build -f docker/Dockerfile -t hanbit-erp:latest .
#
# 실행 (MySQL은 docker-compose.yml로 별도 기동, 같은 네트워크에 연결):
#   docker compose up -d                      # hanbit-erp-mysql 컨테이너 + <project>_default 네트워크 기동
#   docker run -d --name hanbit-erp \
#     --network erp_default \
#     -p 8080:8080 \
#     -e JDBC_URL="jdbc:mysql://hanbit-erp-mysql:3306/hanbit_erp?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Seoul" \
#     -e JDBC_USERNAME=erp \
#     -e JDBC_PASSWORD=erp1234 \
#     hanbit-erp:latest
#
# DB 접속 정보는 src/main/resources/db.properties에 로컬 Jetty 개발용 기본값
# (localhost:3309)이 들어있지만, Spring의 <context:property-placeholder>가
# 기본적으로 시스템 프로퍼티/환경변수를 파일보다 우선 적용한다(system-properties-mode
# 기본값 ENVIRONMENT). 그래서 위처럼 JDBC_URL/JDBC_USERNAME/JDBC_PASSWORD 환경변수를
# 주면 WAR를 다시 빌드하지 않고도 배포 환경의 실제 MySQL 호스트로 접속 정보를 바꿀 수
# 있다 — 실제로 이 이미지를 이 방식으로 hanbit-erp-mysql 컨테이너에 연결해 로그인/
# 조직도 조회/기안 작성까지 curl로 검증했다.

# ---- 1단계: WAR 빌드 ----
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY pom.xml .
# 의존성만 먼저 받아 레이어 캐시를 최대한 활용
RUN mvn -q -B dependency:go-offline || true
COPY src ./src
RUN mvn -q -B clean package -DskipTests

# ---- 2단계: Tomcat 9 런타임 ----
# Tomcat 7.0.47(Servlet 3.0)은 Spring Security의 HttpSessionEventPublisher가 요구하는
# HttpSessionIdListener(Servlet 3.1+)를 제공하지 못해 로컬 개발에서도 이미 배제한 조합이다
# (docs/troubleshooting.md). Tomcat 9는 Servlet 4.0을 구현해 이 문제가 없고, web.xml도
# 이번 검증을 계기로 실제 요구 버전(4.0)에 맞춰 올렸다.
FROM tomcat:9-jdk17
RUN rm -rf /usr/local/tomcat/webapps/ROOT
COPY --from=build /workspace/target/erp.war /usr/local/tomcat/webapps/ROOT.war
EXPOSE 8080
