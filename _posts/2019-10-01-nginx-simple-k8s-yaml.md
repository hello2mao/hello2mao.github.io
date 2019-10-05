---
layout: post
title: "nginx simple k8s yaml"
subtitle: "nginx simple k8s yaml"
date: 2019-10-01 13:19:00
author: "hello2mao"
hidden: true
tags:
  - cloud native
---

<!-- TOC -->

- [1. nginx.yml](#1-nginxyml)
- [2. nginx_with_app_conf.yaml](#2-nginxwithappconfyaml)
- [3. nginx_with_basic_auth.yaml](#3-nginxwithbasicauthyaml)

<!-- /TOC -->

# 1. nginx.yml

```yml
---
kind: Service
apiVersion: v1
metadata:
  name: nginx-service
spec:
  selector:
    app: nginx
  type: LoadBalancer
  ports:
    - name: nginx-port
      port: 80
      targetPort: 80
      protocol: TCP
---
apiVersion: apps/v1beta1
kind: Deployment
metadata:
  name: nginx-deployment
spec:
  replicas: 1
  template:
    metadata:
      labels:
        app: nginx
    spec:
      containers:
        - name: nginx
          image: nginx
          ports:
            - containerPort: 80
```

# 2. nginx_with_app_conf.yaml

```yml
---
apiVersion: v1
kind: ConfigMap
metadata:
  name: nginx-conf
data:
  nginx.conf: |-
    user  nginx;
    worker_processes  1;
    error_log  /var/log/nginx/error.log warn;
    pid        /var/run/nginx.pid;
    events {
        worker_connections  1024;
    }
    http {
        include       /etc/nginx/mime.types;
        default_type  application/octet-stream;
        log_format  main  '$remote_addr - $remote_user [$time_local] "$request" '
                          '$status $body_bytes_sent "$http_referer" '
                          '"$http_user_agent" "$http_x_forwarded_for"';
        access_log  /var/log/nginx/access.log  main;
        sendfile        on;
        keepalive_timeout  65;
        server {
            listen       8080;
            server_name  localhost;
            root   /usr/share/nginx/html;
            index index.html;
        }
    }
---
kind: Service
apiVersion: v1
metadata:
  name: nginx-service
spec:
  selector:
    app: nginx
  type: LoadBalancer
  ports:
    - name: http
      port: 80
      targetPort: 8080
---
apiVersion: v1
kind: ReplicationController
metadata:
  name: nginx-rc
spec:
  replicas: 1
  template:
    metadata:
      labels:
        app: nginx
      namespace: default
    spec:
      containers:
        - name: nginx
          image: hub.baidubce.com/jpaas-public/nginx:latest
          ports:
            - containerPort: 8080
          volumeMounts:
            - name: nginx-conf
              mountPath: /etc/nginx/nginx.conf
              subPath: nginx.conf
      volumes:
        - name: nginx-conf
          configMap:
            name: nginx-conf
            items:
              - key: nginx.conf
                path: nginx.conf
```

# 3. nginx_with_basic_auth.yaml

```yml
---
apiVersion: v1
kind: ConfigMap
metadata:
  name: nginx-conf
data:
  nginx.conf: |-
    user  nginx;
    worker_processes  1;
    error_log  /var/log/nginx/error.log warn;
    pid        /var/run/nginx.pid;
    events {
        worker_connections  1024;
    }
    http {
        include       /etc/nginx/mime.types;
        default_type  application/octet-stream;
        log_format  main  '$remote_addr - $remote_user [$time_local] "$request" '
                          '$status $body_bytes_sent "$http_referer" '
                          '"$http_user_agent" "$http_x_forwarded_for"';
        access_log  /var/log/nginx/access.log  main;
        sendfile        on;
        keepalive_timeout  65;
        server {
            listen       8080;
            server_name  localhost;
            root   /usr/share/nginx/html;
            index index.html;
            auth_basic   "登录认证"; 
            auth_basic_user_file /etc/nginx/pw.db;
        }
    }
---
apiVersion: v1
kind: ConfigMap
metadata:
  name: nginx-pw
data:
  pw.db: |-
    admin:$apr1$rhy1LrNs$LZVGU9Ybl6KgiUpTl8GYH0
---
kind: Service
apiVersion: v1
metadata:
  name: nginx-service
spec:
  selector:
    app: nginx
  type: LoadBalancer
  ports:
    - name: http
      port: 80
      targetPort: 8080
---
apiVersion: v1
kind: ReplicationController
metadata:
  name: nginx-rc
spec:
  replicas: 1
  template:
    metadata:
      labels:
        app: nginx
      namespace: default
    spec:
      containers:
        - name: nginx
          image: hub.baidubce.com/jpaas-public/nginx:latest
          ports:
            - containerPort: 8080
          volumeMounts:
            - name: nginx-conf
              mountPath: /etc/nginx/nginx.conf
              subPath: nginx.conf
            - name: nginx-pw
              mountPath: /etc/nginx/pw.db
              subPath: pw.db
      volumes:
        - name: nginx-conf
          configMap:
            name: nginx-conf
            items:
              - key: nginx.conf
                path: nginx.conf
        - name: nginx-pw
          configMap:
            name: nginx-pw
            items:
              - key: pw.db
                path: pw.db
```
