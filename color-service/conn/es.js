const elasticsearch = require('elasticsearch')

const esHost = process.env.ES_HOST || 'localhost'

const client = new elasticsearch.Client({
    host: `${esHost}:9200`,
  //log: 'trace'
})

module.exports = client

