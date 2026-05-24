# Frontend je statički (HTML/CSS/JS) pa ga samo poslužimo preko nginxa.
FROM nginx:alpine

# Vlastita nginx konfiguracija (SPA-friendly, cache isključen za demo)
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Kopiramo sve statičke datoteke u nginx web root
COPY . /usr/share/nginx/html

EXPOSE 80
