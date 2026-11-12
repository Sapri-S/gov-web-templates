# Build from the repository root:
#   docker build -f deploy/web.Dockerfile -t portal-web .
FROM node:25-alpine AS build
WORKDIR /src
COPY apps/portal-web/package.json apps/portal-web/package-lock.json ./
RUN npm ci
COPY apps/portal-web/ ./
RUN npm run build

FROM nginxinc/nginx-unprivileged:1-alpine
COPY deploy/nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /src/dist /usr/share/nginx/html
EXPOSE 8080
