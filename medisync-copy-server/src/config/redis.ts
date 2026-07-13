import Redis from 'ioredis'
import dotenv from 'dotenv'
dotenv.config()

const redisUrl = process.env.REDIS_URL || 'redis://localhost:6379'


export const publisher = new Redis(redisUrl)
export const subscriber = new Redis(redisUrl)

publisher.on('connect', () => console.log('Redis publisher connected'))
subscriber.on('connect', () => console.log('Redis subscriber connected'))

publisher.on('error', (err) => console.error('Redis publisher error:', err))
subscriber.on('error', (err) => console.error('Redis subscriber error:', err))