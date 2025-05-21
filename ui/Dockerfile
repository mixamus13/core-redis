FROM node:20-alpine
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
EXPOSE 3000
ENV API_BASE=http://localhost:8087/api/v1
ENV REDIS_HOST=redis
CMD ["npm", "start"]
