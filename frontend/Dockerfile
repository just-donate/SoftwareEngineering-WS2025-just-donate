FROM node:18-alpine

RUN addgroup -S nonroot \
    && adduser -S nonroot -G nonroot

USER nonroot

WORKDIR /app

COPY package*.json ./

RUN npm install --ignore-scripts

RUN apk add --no-cache git

COPY . .

RUN npm run predeploy

CMD ["npm", "run", "dev"]
