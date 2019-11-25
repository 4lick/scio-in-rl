const express = require('express')
const router = express.Router()
const User = require('../models/User')
const es = require('../conn/es')

router.get('/:source?/:id?', function(req, res, next) {
  const id = req.params.id
  const source = req.params.source

  const colors = [
    "red",
    "orange",
    "yellow",
    "green",
    "blue",
    "indigo",
    "violet"
  ]
  const sourcesRef = ["mobile", "site"]

  const color = colors[Math.floor(Math.random() * colors.length)]
  //const source = sourcesRef[Math.floor(Math.random() * sourcesRef.length)]
  const now = new Date().toISOString()

  if
   (id && source) {
     if (!sourcesRef.includes(source)) {
       res.status(404)
       return res.json({error: "source error"})
     }

    User.getUserById(id, function(err, rows) {
      if (err) {
        res.json(err)
      } else if (rows.length) {
        const event = {
          color: color,
          source: source,
          user_id: id,
          created: now,
        }

        es.index({
          index: 'color',
          type: 'event',
          body: event
        }, function(err, resp, status) {
             if (err) {
               console.log(resp);
               res.status(500)
               res.json({error: "event not performed"})
             } else {
               res.json(event)
             }
        })
      } else {
          res.status(404)
          res.json({error: "user not found"})
      }
    })
  } else {
      res.status(404)
      res.json({error: "source or user id error"})
  }
})

module.exports = router
