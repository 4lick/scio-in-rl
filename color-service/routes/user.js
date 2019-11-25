const express = require('express')
const router = express.Router()
const User = require('../models/User')

router.get('/:id?', function(req, res, next) {
  if (req.params.id) {
    User.getUserById(req.params.id, function(err, rows) {
      if (err) {
        res.json(err)
      } else {
        res.json(rows[0])
      }
    })
  }
})

module.exports = router
