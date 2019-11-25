const express = require('express')
const path = require('path')
const bodyParser = require('body-parser')
const cors = require('cors')
const Users = require('./routes/user')
const Colors = require('./routes/color')

const app = express()

app.use(cors())
app.use(bodyParser.json())
app.use(bodyParser.urlencoded({extended: false}))

app.use('/user', Users)
app.use('/color', Colors)

// catch 404 and forward to error handler
app.use(function(req, res, next) {
  const err = new Error('Not Found')
  err.status = 404
  next(err)
})

// error handlers
app.use(function(err, req, res, next) {
  res.status(err.status || 500)
  res.render('error', {
    message: err.message,
    error: err
  })
})

module.exports = app
