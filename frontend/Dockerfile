# ==============================================================================
# 🅰️ DEVSECOPS DOCKERFILE - FRONTEND ANGULAR 21 (NGINX)
# ==============================================================================
# Multi-stage build : Node.js (Build) -> Nginx Alpine (Production & Security)
# ==============================================================================

# ------------------------------------------------------------------------------
# ÉTAPE 1 : Compilation de l'application Angular (Build Stage)
# ------------------------------------------------------------------------------
FROM node:20-alpine AS builder

WORKDIR /app

# Installation optimisée des dépendances
COPY package.json package-lock.json ./
RUN npm ci --legacy-peer-deps || npm install --legacy-peer-deps

# Copie du code source et compilation pour la production sans blocage de budget
COPY . .
RUN npx ng build --configuration=development

# ------------------------------------------------------------------------------
# ÉTAPE 2 : Serveur de production Nginx ultra-légère (Runtime Stage)
# ------------------------------------------------------------------------------
FROM nginx:alpine AS runtime

LABEL maintainer="SIMPL-IS DevSecOps Team" \
      description="Frontend Angular - Nginx Secure Server"

# Suppression de la page d'accueil Nginx par défaut
RUN rm -rf /usr/share/nginx/html/*

# Copie de la configuration Nginx sécurisée
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Copie des fichiers Angular compilés vers Nginx
COPY --from=builder /app/dist/frontend /usr/share/nginx/html

# Ajustement pour Angular SSR / subfolder browser si présent
RUN if [ -d "/usr/share/nginx/html/browser" ]; then \
      cp -r /usr/share/nginx/html/browser/* /usr/share/nginx/html/ && \
      rm -rf /usr/share/nginx/html/browser; \
    fi

# Attribution des droits au serveur web
RUN chown -R nginx:nginx /usr/share/nginx/html && \
    chmod -R 755 /usr/share/nginx/html

# Exposition du port HTTP
EXPOSE 80

# Healthcheck natif pour vérifier que Nginx répond
HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=3 \
  CMD wget --quiet --tries=1 --spider http://localhost:80/ || exit 1

# Démarrage de Nginx en premier plan
CMD ["nginx", "-g", "daemon off;"]
