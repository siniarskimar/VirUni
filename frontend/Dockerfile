FROM node:25-alpine

RUN apk add pnpm

WORKDIR /home/app

COPY --chown=app:app package*.json pnpm-lock.yaml ./
RUN --mount=type=cache,target=/home/app/node_modules \
  pnpm install --frozen-lockfile

COPY --chown=app:app . .
RUN --mount=type=cache,target=/home/app/node_modules \
  pnpm run build
