FROM gradle:jdk17 AS build

WORKDIR /app
COPY --chown=gradle:gradle . .

RUN gradle build
ENV GOOGLE_APPLICATION_CREDENTIALS=/app/automod.json

CMD ["gradle", "run"]
