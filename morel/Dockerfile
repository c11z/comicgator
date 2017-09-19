FROM nginx:latest

LABEL author mr@comicgator.com

COPY dist /data/www
COPY nginx.conf /etc/nginx/nginx.conf

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
