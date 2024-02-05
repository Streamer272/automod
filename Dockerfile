FROM gradle:jdk17

WORKDIR /app
COPY --chown=gradle:gradle . .

RUN gradle build

CMD ["gradle", "run"]
