import Redis from 'ioredis'
import dotenv from 'dotenv'
dotenv.config()

const redisConfig = {
    host: process.env.REDIS_HOST || 'localhost',
    port: Number(process.env.REDIS_PORT) || 6379,
    password: process.env.REDIS_PASSWORD || 'medisync1234',
}


export const publisher = new Redis(redisConfig)


export const subscriber = new Redis(redisConfig)

publisher.on('connect', () => console.log('Redis publisher connected'))
subscriber.on('connect', () => console.log('Redis subscriber connected'))

publisher.on('error', (err) => console.error('Redis publisher error:', err))
subscriber.on('error', (err) => console.error('Redis subscriber error:', err))