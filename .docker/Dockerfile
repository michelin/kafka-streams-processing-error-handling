FROM eclipse-temurin:25-jre-alpine

LABEL org.opencontainers.image.authors="Michelin"

COPY target/kafka-streams-processing-error-handling.jar /app/kafka-streams-processing-error-handling.jar

RUN apk update \
    && echo -e "#!/bin/sh\n\njava -jar /app/kafka-streams-processing-error-handling.jar \"\$@\"" > /usr/bin/kafka-streams-processing-error-handling \
    && chmod +x /usr/bin/kafka-streams-processing-error-handling \
    && apk upgrade \
    && rm -rf /var/cache/apk/*

ENTRYPOINT ["kafka-streams-processing-error-handling"]