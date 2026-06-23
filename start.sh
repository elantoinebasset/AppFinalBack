#!/usr/bin/env bash
#
# Script de gestion de l'application Quarkus Scheduler
# Usage: ./start.sh [commande]
#
set -euo pipefail

cd "$(dirname "$0")"

# Couleurs
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

print_help() {
    echo -e "${BLUE}=== Quarkus Scheduler - Gestion de l'application ===${NC}"
    echo ""
    echo "Usage: ./start.sh [commande]"
    echo ""
    echo "Commandes disponibles:"
    echo -e "  ${GREEN}prod${NC}      Démarre toute la stack avec Docker (API + PostgreSQL)"
    echo -e "  ${GREEN}dev${NC}       Démarre le mode développement (hot-reload) + PostgreSQL"
    echo -e "  ${GREEN}stop${NC}      Arrête tous les conteneurs Docker"
    echo -e "  ${GREEN}clean${NC}     Arrête tout ET supprime les données de la base"
    echo -e "  ${GREEN}logs${NC}      Affiche les logs de l'API en direct"
    echo -e "  ${GREEN}status${NC}    Affiche l'état des conteneurs"
    echo -e "  ${GREEN}test${NC}      Teste l'endpoint de santé de l'API"
    echo -e "  ${GREEN}help${NC}      Affiche cette aide"
    echo ""
    echo "Sans argument, 'prod' est utilisé par défaut."
}

case "${1:-prod}" in
    prod)
        echo -e "${BLUE}>> Démarrage de la stack complète (Docker)...${NC}"
        docker-compose up -d --build
        echo -e "${GREEN}✔ Application disponible sur http://localhost:8080${NC}"
        echo -e "  Health check: http://localhost:8080/api/health"
        ;;

    dev)
        echo -e "${BLUE}>> Démarrage du mode développement...${NC}"
        echo -e "${YELLOW}>> Arrêt de l'API Docker pour libérer le port 8080...${NC}"
        docker-compose stop scheduler-api 2>/dev/null || true
        echo -e "${YELLOW}>> Démarrage de PostgreSQL...${NC}"
        docker-compose up -d postgres
        echo -e "${GREEN}>> Lancement de Quarkus en mode dev (hot-reload)...${NC}"
        echo -e "${YELLOW}   (Ctrl+C ou 'q' pour quitter)${NC}"
        ./mvnw quarkus:dev
        ;;

    stop)
        echo -e "${YELLOW}>> Arrêt des conteneurs...${NC}"
        docker-compose stop
        echo -e "${GREEN}✔ Conteneurs arrêtés${NC}"
        ;;

    clean)
        echo -e "${RED}>> Arrêt et suppression des données...${NC}"
        docker-compose down -v
        echo -e "${GREEN}✔ Tout est nettoyé${NC}"
        ;;

    logs)
        docker-compose logs -f scheduler-api
        ;;

    status)
        echo -e "${BLUE}>> État des conteneurs:${NC}"
        docker-compose ps
        ;;

    test)
        echo -e "${BLUE}>> Test de l'API...${NC}"
        curl -s http://localhost:8080/api/health || echo -e "${RED}API non accessible${NC}"
        echo ""
        ;;

    help | -h | --help)
        print_help
        ;;

    *)
        echo -e "${RED}Commande inconnue: $1${NC}"
        echo ""
        print_help
        exit 1
        ;;
esac
