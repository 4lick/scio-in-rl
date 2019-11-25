This project contains Dockerfile that creates a base image to run color service.

How to use this image
=====================

NodeJS Applications
-------------------

Build:

    cd color-service
    docker build -t 4lick/color .
    docker build -t gcr.io/your-project/color:v1 .
    docker run -d -p 3000:3000 gcr.io/your-project/color:v1
    gcloud docker -- push gcr.io/your-project/color:v1

Service:
	curl localhost:3000/color/mobile/1049
	curl localhost:3000/user/1049
