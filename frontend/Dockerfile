FROM node:22-alpine AS build
WORKDIR /app
COPY package.json angular.json tsconfig.json tsconfig.app.json ./
RUN npm install
COPY src ./src
RUN npm run build

FROM nginx:1.27-alpine
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist/pendencias-frontend/browser /usr/share/nginx/html
EXPOSE 80
