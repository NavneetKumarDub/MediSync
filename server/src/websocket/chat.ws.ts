import { WebSocketServer, WebSocket } from 'ws'
import { Server } from 'http'
import { URL } from 'url'
import jwt from 'jsonwebtoken'
import { publisher, subscriber } from '../config/redis'
import db from '../config/db'

interface Socket extends WebSocket {
    userId: number
    role: 'doctor' | 'patient'
    channels: Set<string>
    isAlive: boolean
}

const channelRefs = new Map<string, Set<Socket>>()
const userSockets = new Map<number, Set<Socket>>()

function send(ws: WebSocket, type: string, data: unknown) {
    if (ws.readyState === WebSocket.OPEN) {
        ws.send(JSON.stringify({ type, data }))
    }
}

async function subscribe(ws: Socket, channel: string) {
    if (!channelRefs.has(channel)) {
        channelRefs.set(channel, new Set())
        await subscriber.subscribe(channel)
    }
    channelRefs.get(channel)!.add(ws)
    ws.channels.add(channel)
}

async function unsubscribe(ws: Socket, channel: string) {
    const set = channelRefs.get(channel)
    if (!set) return
    set.delete(ws)
    ws.channels.delete(channel)
    if (set.size === 0) {
        channelRefs.delete(channel)
        await subscriber.unsubscribe(channel)
    }
}

export async function sendToUser(userId: number, type: string, data: unknown) {
    await publisher.publish(`user:${userId}`, JSON.stringify({ type, data }))
}

export function isUserOnline(userId: number) {
    return (userSockets.get(userId)?.size ?? 0) > 0
}

async function isInRoom(userId: number, roomId: number) {
    const r = await db.query(
        `SELECT id FROM chat_rooms WHERE id = $1 AND (patient_id = $2 OR doctor_id = $2)`,
        [roomId, userId]
    )
    return r.rows.length > 0
}

async function loadHistory(roomId: number) {
    const cached = await publisher.get(`chat:history:${roomId}`)
    if (cached) return JSON.parse(cached)

    const r = await db.query(
        `SELECT id, sender_id, message, sent_at, is_read
         FROM chat_messages WHERE room_id = $1
         ORDER BY sent_at DESC LIMIT 50`,
        [roomId]
    )
    const messages = r.rows.reverse()
    await publisher.setex(`chat:history:${roomId}`, 60, JSON.stringify(messages))
    return messages
}

async function handleMessage(ws: Socket, type: string, data: any) {
    const uid = ws.userId

    switch (type) {
        case 'chat:join': {
            if (!(await isInRoom(uid, data.roomId))) {
                return send(ws, 'error', { message: 'Access denied' })
            }
            await subscribe(ws, `chat:room:${data.roomId}`)
            send(ws, 'chat:joined', { roomId: data.roomId })
           
            break
        }

        case 'chat:leave': {
            await unsubscribe(ws, `chat:room:${data.roomId}`)
            break
        }

        case 'chat:message': {
            const channel = `chat:room:${data.roomId}`
            if (!ws.channels.has(channel)) {
                return send(ws, 'error', { message: 'Join room first' })
            }

            const r = await db.query(
                `INSERT INTO chat_messages (room_id, sender_id, message)
                 VALUES ($1, $2, $3) RETURNING id, sent_at`,
                [data.roomId, uid, data.text]
            )
            await publisher.del(`chat:history:${data.roomId}`)
            await publisher.publish(channel, JSON.stringify({
                type: 'chat:message',
                data: {
                    messageId: r.rows[0].id,
                    roomId: data.roomId,
                    senderId: uid,
                    text: data.text,
                    sentAt: r.rows[0].sent_at,
                },
            }))
            break
        }

        case 'chat:read': {
            await db.query(
                `UPDATE chat_messages SET is_read = true WHERE id = $1 AND room_id = $2`,
                [data.messageId, data.roomId]
            )
            await publisher.publish(`chat:room:${data.roomId}`, JSON.stringify({
                type: 'chat:read',
                data: { messageId: data.messageId },
            }))
            break
        }

        default:
            send(ws, 'error', { message: `Unknown type: ${type}` })
    }
}

export function initChatWebSocket(server: Server) {
    const wss = new WebSocketServer({ noServer: true })

    subscriber.on('message', (channel, payload) => {
        channelRefs.get(channel)?.forEach(ws => {
            if (ws.readyState === WebSocket.OPEN) ws.send(payload)
        })
    })

    server.on('upgrade', (req, socket, head) => {
        try {
            const url = new URL(req.url ?? '/', `http://${req.headers.host}`)
             if (url.pathname !== '/chat') return

            const token = url.searchParams.get('token')
            if (!token) {
                socket.write('HTTP/1.1 401 Unauthorized\r\n\r\n')
                return socket.destroy()
            }

            const payload = jwt.verify(token, process.env.JWT_SECRET!) as {
                id: number
                role: 'doctor' | 'patient'
            }

            wss.handleUpgrade(req, socket, head, ws => {
                const s = ws as Socket
                s.userId = payload.id
                s.role = payload.role
                s.channels = new Set()
                s.isAlive = true
                wss.emit('connection', s)
            })
        } catch {
            socket.write('HTTP/1.1 401 Unauthorized\r\n\r\n')
            socket.destroy()
        }
    })

    wss.on('connection', async (ws: Socket) => {
        const uid = ws.userId

        if (!userSockets.has(uid)) userSockets.set(uid, new Set())
        userSockets.get(uid)!.add(ws)

        await subscribe(ws, `user:${uid}`)
        send(ws, 'connected', { userId: uid })

        ws.on('pong', () => { ws.isAlive = true })

        ws.on('message', async raw => {
            try {
                const { type, data = {} } = JSON.parse(raw.toString())
                await handleMessage(ws, type, data)
            } catch (err) {
                console.error('handler error:', err)
                send(ws, 'error', { message: 'Invalid message' })
            }
        })

        ws.on('close', async () => {
            for (const ch of [...ws.channels]) await unsubscribe(ws, ch)
            userSockets.get(uid)?.delete(ws)
            if (userSockets.get(uid)?.size === 0) userSockets.delete(uid)
        })
    })

    setInterval(() => {
        wss.clients.forEach(client => {
            const ws = client as Socket
            if (!ws.isAlive) return ws.terminate()
            ws.isAlive = false
            ws.ping()
        })
    }, 30_000)

    console.log('WebSocket ready at /ws')
}