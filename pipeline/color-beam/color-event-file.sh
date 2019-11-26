#!/bin/bash

START_DATE=`date '+%Y-%m-%d'`

sbt "run-main me.a4lick.beam.jobs.ColorEventFileToBq \
--runner=DirectRunner
--streaming=false
--project=bustling-folio-241315
--fileInput=gs://4lick/demo/events.json
--zone=europe-west1-d
--exitAfterSubmit=true
--keepJobsRunning=true
--jobName=color-event
--workerMachineType=n1-standard-4
--numWorkers=1
--maxNumWorkers=1
--autoscalingAlgorithm=THROUGHPUT_BASED
--apiHost=localhost
--apiPort=3000
--bigQueryProjectId=bustling-folio-241315
--bigQueryDataset=DEMO
--bigQueryTable=color_event
--workerLogLevelOverrides={\"fr.figarocms\":\"DEBUG\"}"