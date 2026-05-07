import { Pool,types } from 'pg'
import dotenv from 'dotenv'

dotenv.config()

types.setTypeParser(1082, (val: string) => val)


const db = new Pool({
    host: process.env.DB_HOST,
    port: Number(process.env.DB_PORT),
    database: process.env.DB_NAME,
    user: process.env.DB_USER,
    password: process.env.DB_PASSWORD,
})

export default db