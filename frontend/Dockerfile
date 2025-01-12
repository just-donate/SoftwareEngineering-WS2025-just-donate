FROM node:18-alpine

WORKDIR /app

COPY package*.json ./

RUN npm install --ignore-scripts

RUN apk add --no-cache git

COPY . .

RUN npm run predeploy

CMD ["npm", "run", "dev"]
