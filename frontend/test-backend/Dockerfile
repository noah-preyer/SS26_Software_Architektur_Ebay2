FROM node:22-alpine

WORKDIR /app

# Keine npm-Abhängigkeiten - der Server nutzt nur Node-Bordmittel. package.json
# wird nur fürs Image-Metadaten/Start mitkopiert.
COPY package.json ./
COPY server.js seed.js ./

# Persistenz landet auf einem gemounteten Volume (siehe docker-compose),
# damit Daten Container-Neustarts überleben.
ENV PORT=8090
ENV DATA_FILE=/data/data.json

EXPOSE 8090

CMD ["node", "server.js"]
