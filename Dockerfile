FROM eclipse-temurin:17-jre-alpine

# /app 디렉토리 생성
WORKDIR /app

# JAR 파일 복사 (빌드 컨텍스트의 target 디렉토리에서)
COPY back-end-0.0.1-SNAPSHOT.jar /app/test.jar

# 포트 노출 (선택 사항)
EXPOSE 8080

# 컨테이너 실행 시 JAR 파일을 실행
CMD ["java", "-jar", "/app/test.jar"]

