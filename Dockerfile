FROM gradle:jdk17 AS build
COPY --chown=gradle:gradle . /app
WORKDIR /app
RUN gradle build
ENV GOOGLE_APPLICATION_CREDENTIALS=/app/gcloud.json
EXPOSE 8080
CMD ["gradle", "run"]
