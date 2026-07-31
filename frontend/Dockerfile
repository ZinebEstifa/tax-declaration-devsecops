# ==============================================================================
# 🅰️ DEVSECOPS DOCKERFILE - FRONTEND ANGULAR 19 (NGINX)
# ==============================================================================
# Multi-stage build : Node.js (Build) -> Nginx Alpine (Production & Security)
# ==============================================================================

# ------------------------------------------------------------------------------
# ÉTAPE 1 : Compilation de l'application Angular (Build Stage)
# ------------------------------------------------------------------------------
FROM node:18-alpine AS builder

WORKDIR /app

# Installation optimisée des dépendances avec cache Docker
COPY package.json package-lock.json ./
RUN npm ci

# Copie du code source et compilation pour la production
COPY . .
RUN npm run build -- --configuration=production

# ------------------------------------------------------------------------------
# ÉTAPE 2 : Serveur de production Nginx ultra-légère (Runtime Stage)
# ------------------------------------------------------------------------------
FROM nginx:alpine AS runtime

LABEL maintainer="SIMPL-IS DevSecOps Team" \
      description="Frontend Angular 19 - Nginx Secure Server"

# Suppression de la page d'accueil Nginx par défaut
RUN rm -rf /usr/share/nginx/html/*

# Copie de la configuration Nginx sécurisée
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Copie des fichiers Angular compilés depuis l'étape de build
# (Note: Angular 19 génère la sortie dans dist/frontend/browser)
COPY --from=builder /app/dist/frontend/browser /usr/share/nginx/html

# Attribution des droits au serveur web
RUN chown -R nginx:nginx /usr/share/nginx/html && \
    chmod -R 755 /usr/share/nginx/html

# Exposition du port HTTP
EXPOSE 80

# Healthcheck natif pour vérifier que Nginx réponds
HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=3 \
  CMD wget --quiet --tries=1 --spider http://localhost:80/ || exit 1

# Démarrage de Nginx en premier plan
CMD ["nginx", "-g", "daemon off;"]
