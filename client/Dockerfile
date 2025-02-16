FROM node:22.13.1

WORKDIR /app

COPY . .

RUN npm install

EXPOSE 443
CMD [ "npm", "run", "dev" ]
