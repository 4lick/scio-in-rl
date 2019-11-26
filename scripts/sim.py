#!/usr/bin/env python

import time
import pytz
import argparse
import datetime
import random
import json
from google.auth import jwt
from google.cloud import pubsub_v1

service_account_info = json.load(open("./credentials.json"))
audience = "https://pubsub.googleapis.com/google.pubsub.v1.Subscriber"

credentials = jwt.Credentials.from_service_account_info(
    service_account_info, audience=audience
)

subscriber = pubsub_v1.SubscriberClient(credentials=credentials)

publisher_audience = "https://pubsub.googleapis.com/google.pubsub.v1.Publisher"
credentials_pub = credentials.with_claims(audience=publisher_audience)
publisher = pubsub_v1.PublisherClient(credentials=credentials_pub)

RFC3339_TIME_FORMAT = '%Y-%m-%dT%H:%M:%S-00:00'

colors = [ "red", "orange", "yellow", "green", "blue", "indigo", "violet" ]
source = [ "site", "mobile" ]

def get_event():
     event = json.dumps({
          'color': random.choice(colors),
          'source': random.choice(source),
          'created': datetime.datetime.utcnow().strftime(RFC3339_TIME_FORMAT),
          'user_id': random.randint(1001,1099)
     })
     return event

if __name__ == '__main__':
   parser = argparse.ArgumentParser(description='Send Color Events to Pub/Sub')
   parser.add_argument('--project', help='project id', required=True)
   parser.add_argument('--nbMsg', help='Generated Messages Number', required=True)
   parser.add_argument('--topic', help='Pub/Sub Topic', required=True)

   # 
   args = parser.parse_args()

   topic_name = args.topic
   project_id = args.project
   nbMsg = int(args.nbMsg)

   # 
   startTime = datetime.datetime.utcnow().strftime(RFC3339_TIME_FORMAT)
   print(f'Start : {startTime}')
   print(f'Nb Msg : {args.nbMsg:10}\n')
   
   topic_path = publisher.topic_path(project_id, topic_name)

   i = 0   
   while i < nbMsg:          
     event = get_event()
     data = event.encode('utf-8')   
     publisher.publish(topic_path, data, event_type='ColorEvent')      

     print(f'=> event : {event}')
     i += 1
