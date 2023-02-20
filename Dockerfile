FROM gradle:jdk17 AS build
COPY --chown=gradle:gradle . /app
WORKDIR /app
RUN gradle build
ENV GOOGLE_APPLICATION_CREDENTIALS=/app/gcloud.json
ENTRYPOINT ["gradle", "run"]
