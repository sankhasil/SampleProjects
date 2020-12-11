use admin

db.createUser({ user: "admin", pwd: "admin", roles: [{ role: "dbAdminAnyDatabase", db: "admin" }] })