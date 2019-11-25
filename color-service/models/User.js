const db = require('../conn/db')

const User = {
  getUserById: function(id, callback) {
    return db.query("select * from user where id=?", [id], callback)
  }
}

module.exports = User
