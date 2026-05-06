# Étape 1 : build Angular
FROM node:18 AS build

WORKDIR /app

COPY package*.json ./

# FIX CONFLITS DEPENDENCIES
RUN npm install --legacy-peer-deps

COPY . .

RUN npm run build -- --configuration production


# Étape 2 : serveur Nginx
FROM nginx:alpine

# COPY du bon dossier Angular (IMPORTANT)
COPY --from=build /app/dist/school-management /usr/share/nginx/html

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]