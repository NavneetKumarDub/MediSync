import { WebSocketServer, WebSocket } from 'ws'
import { Server } from 'http'
import { URL } from 'url'
import * as crypto from 'crypto'
import { VideoSignal } from '../types/video.types'



const socketToWsMap = new Map<string, WebSocket>()
const roomToSocketsMap = new Map<number, Set<string>>()



const safeSend = (ws: WebSocket, payload: object): void => {
    if (ws.readyState === WebSocket.OPEN) {
        ws.send(JSON.stringify(payload))
    }
}

const broadcastToRoom = (roomId: number, senderSocketId: string, payload: object): void => {
    const socketsInRoom = roomToSocketsMap.get(roomId)
    if (!socketsInRoom) return

    socketsInRoom.forEach((socketId) => {
        if (socketId === senderSocketId) return
        const ws = socketToWsMap.get(socketId)
        if (ws) safeSend(ws, payload)
    })
}

const leaveRoom = (socketId: string, roomId: number): void => {
    const socketsInRoom = roomToSocketsMap.get(roomId)
    if (!socketsInRoom) return

    socketsInRoom.delete(socketId)

    broadcastToRoom(roomId, socketId, {
        type: 'user-left',
        roomId,
        id: socketId,
    })

    if (socketsInRoom.size === 0) {
        roomToSocketsMap.delete(roomId)
        console.log(`[Video] Room ${roomId} deleted (empty).`)
    }
}



const handleVideoSignaling = (ws: WebSocket): void => {
    const socketId = crypto.randomUUID()
    socketToWsMap.set(socketId, ws)

    const joinedRooms = new Set<number>()

    console.log(`[Video] Connected: ${socketId}`)

    ws.on('message', (raw: Buffer | string) => {
        try {
            const signal: VideoSignal = JSON.parse(raw.toString())
            const { type, roomId } = signal

            switch (type) {
                case 'join-room': {
                    if (!roomId) break

                    if (!roomToSocketsMap.has(roomId)) {
                        roomToSocketsMap.set(roomId, new Set())
                    }

                    roomToSocketsMap.get(roomId)!.add(socketId)
                    joinedRooms.add(roomId)

                    console.log(`[Video] ${socketId} joined room ${roomId}`)

                    broadcastToRoom(roomId, socketId, {
                        type: 'user-joined',
                        roomId,
                        id: socketId,
                    })

                    safeSend(ws, {
                        type: 'room-joined',
                        roomId,
                        id: socketId,
                    })

                    break
                }

                case 'leave-room': {
                    if (!roomId) break
                    leaveRoom(socketId, roomId)
                    joinedRooms.delete(roomId)
                    console.log(`[Video] ${socketId} left room ${roomId}`)
                    break
                }

                case 'offer': {
                    const payload = { type: 'offer', roomId, id: socketId, sdp: signal.sdp }

                    if (signal.targetId) {
                        const targetWs = socketToWsMap.get(signal.targetId)
                        if (!targetWs) { console.warn(`[Video] Target not found for offer: ${signal.targetId}`); break }
                        safeSend(targetWs, payload)
                    } else {
                        broadcastToRoom(roomId, socketId, payload)
                    }
                    break
                }

                case 'answer': {
                    const payload = { type: 'answer', roomId, id: socketId, sdp: signal.sdp }

                    if (signal.targetId) {
                        const targetWs = socketToWsMap.get(signal.targetId)
                        if (!targetWs) { console.warn(`[Video] Target not found for answer: ${signal.targetId}`); break }
                        safeSend(targetWs, payload)
                    } else {
                        broadcastToRoom(roomId, socketId, payload)
                    }
                    break
                }

                case 'renegotiate': {
                    const payload = { type: 'renegotiate', roomId, id: socketId, sdp: signal.sdp }

                    if (signal.targetId) {
                        const targetWs = socketToWsMap.get(signal.targetId)
                        if (!targetWs) { console.warn(`[Video] Target not found for renegotiate: ${signal.targetId}`); break }
                        safeSend(targetWs, payload)
                    } else {
                        broadcastToRoom(roomId, socketId, payload)
                    }
                    break
                }

                case 'ice-candidate': {
                    const payload = { type: 'ice-candidate', roomId, id: socketId, candidate: signal.candidate }

                    if (signal.targetId) {
                        const targetWs = socketToWsMap.get(signal.targetId)
                        if (!targetWs) { console.warn(`[Video] Target not found for ice-candidate: ${signal.targetId}`); break }
                        safeSend(targetWs, payload)
                    } else {
                        broadcastToRoom(roomId, socketId, payload)
                    }
                    break
                }

                default:
                    console.warn(`[Video] Unknown signal type: ${type}`)
            }
        } catch (err) {
            console.error('[Video] Failed to process message:', err)
        }
    })

    ws.on('close', () => {
        console.log(`[Video] Disconnected: ${socketId}`)
        joinedRooms.forEach((roomId) => leaveRoom(socketId, roomId))
        joinedRooms.clear()
        socketToWsMap.delete(socketId)
    })

    ws.on('error', (err) => {
        console.error(`[Video] Socket error on ${socketId}:`, err)
    })
}



export const initVideoWebSocket = (server: Server): void => {
    const wss = new WebSocketServer({ noServer: true })

    server.on('upgrade', (req, socket, head) => {
        const url = new URL(req.url ?? '/', `http://${req.headers.host}`)

        if (url.pathname !== '/video') return

        wss.handleUpgrade(req, socket, head, (ws) => {
            wss.emit('connection', ws)
        })
    })

    wss.on('connection', (ws) => {
        handleVideoSignaling(ws)
    })

    console.log('Video WebSocket ready at /video')
}