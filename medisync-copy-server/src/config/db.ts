import { Pool, types } from 'pg'
import dotenv from 'dotenv'

dotenv.config()

types.setTypeParser(1082, (val: string) => val)

const db = new Pool({
    connectionString: process.env.DATABASE_URL,
    ssl: { rejectUnauthorized: false },
    max: 10,
    idleTimeoutMillis: 30000,
    connectionTimeoutMillis: 5000,
    keepAlive: true, // <-- Add this to keep connections active
})

// <-- Add this block to catch background connection drops so your server doesn't crash!
db.on('error', (err, client) => {
    console.error('Unexpected error on idle database client:', err.message)
})

export default db