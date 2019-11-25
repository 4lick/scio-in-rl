const mysql = require('mysql')

const dbHost = process.env.DB_HOST || 'localhost'

const connection = mysql.createPool({
   host: dbHost,
   user: 'root',
   password: 'test',
   database: 'color'
})

module.exports = connection
