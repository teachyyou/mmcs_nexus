FROM node:lts-alpine as build

WORKDIR /app
COPY package.json package-lock.json ./
RUN yarn install --production

COPY public ./public
COPY src ./src
RUN yarn build

FROM nginx:1.25.2-alpine

COPY nginx.conf /etc/nginx/conf.d/default.conf

COPY --from=build /app/build /app

EXPOSE 8080

LABEL name="client"