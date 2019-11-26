# "Apache Beam and Google DataFlow IRL with Scala Scio" 

Demo project for Scala.IO 2017 and ParisDataEng' Meetup

* color-service : Color REST API _(NodeJS App)_
* pipeline : scio pipelines _(Batch / Streaming)_
* data : dataset
* scripts : init tools

Prerequisites
=============
- GCP Account
- Python 3.x
- Scala 
- User Account Credentials File

Getting Started _(local run)_
=============================
1. Build color-service image _(see color-service README)_ and Run
```
docker-compose up -d
```

2. load dataset
* load es data
```
- pip install pyes
- cd scripts/ && ./index-data.sh
```

* load mysql data
```
- docker run -it --rm -v $(pwd)/users.sql:/data/users.sql mysql mysql -h172.25.0.1 -uroot -ptest color
- source /data/users.sql
- docker exec -i dev_mysql_1 mysql -uroot -ptest color < users.sql
```

* test url service
```
- curl localhost:3000/color/mobile/1049
- curl localhost:3000/users/1049
```

3. Input File
```
gsutil cp data/events-full.json gs://4lick/demo/events.json
```

4. Pub/Sub Color User Event
```
./scripts/sim.py --project PROJECT_NAME --topic color --nbMsg 5
```

5. pipelines
* The wizard take place...

