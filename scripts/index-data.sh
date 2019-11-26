#!/usr/bin/env bash

# Recreate index with mapping
echo 'Delete index color'
curl -XDELETE localhost:9200/color?pretty
echo 'Recreate index color with mapping'
curl -XPUT localhost:9200/color?pretty --data-binary '@mapping.json'

# Inject data
python3 inject.py
