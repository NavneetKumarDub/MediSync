"use strict";
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.sendToUser = sendToUser;
exports.isUserOnline = isUserOnline;
exports.initChatWebSocket = initChatWebSocket;
const ws_1 = require("ws");
const url_1 = require("url");
const jsonwebtoken_1 = __importDefault(require("jsonwebtoken"));
const redis_1 = require("../config/redis");
const db_1 = __importDefault(require("../config/db"));
const notification_service_1 = require("../services/notification.service");
const channelRefs = new Map();
const userSockets = new Map();
function send(ws, type, data) {
    if (ws.readyState === ws_1.WebSocket.OPEN) {
        ws.send(JSON.stringify({ type, data }));
    }
}
async function subscribe(ws, channel) {
    if (!channelRefs.has(channel)) {
        channelRefs.set(channel, new Set());
        await redis_1.subscriber.subscribe(channel);
    }
    channelRefs.get(channel).add(ws);
    ws.channels.add(channel);
}
async function unsubscribe(ws, channel) {
    const set = channelRefs.get(channel);
    if (!set)
        return;
    set.delete(ws);
    ws.channels.delete(channel);
    if (set.size === 0) {
        channelRefs.delete(channel);
        await redis_1.subscriber.unsubscribe(channel);
    }
}
async function sendToUser(userId, type, data) {
    await redis_1.publisher.publish(`user:${userId}`, JSON.stringify({ type, data }));
}
function isUserOnline(userId) {
    return (userSockets.get(userId)?.size ?? 0) > 0;
}
async function isInRoom(userId, roomId) {
    const r = await db_1.default.query(`SELECT id FROM chat_rooms WHERE id = $1 AND (patient_id = $2 OR doctor_id = $2)`, [roomId, userId]);
    return r.rows.length > 0;
}
async function loadHistory(roomId) {
    const cached = await redis_1.publisher.get(`chat:history:${roomId}`);
    if (cached)
        return JSON.parse(cached);
    const r = await db_1.default.query(`SELECT id, sender_id, message, sent_at, is_read
         FROM chat_messages WHERE room_id = $1
         ORDER BY sent_at DESC LIMIT 50`, [roomId]);
    const messages = r.rows.reverse();
    await redis_1.publisher.setex(`chat:history:${roomId}`, 60, JSON.stringify(messages));
    return messages;
}
async function handleMessage(ws, type, data) {
    const uid = ws.userId;
    switch (type) {
        case 'chat:join': {
            console.log(" joined chat : ", data.roomId);
            if (!(await isInRoom(uid, data.roomId))) {
                return send(ws, 'error', { message: 'Access denied' });
            }
            await subscribe(ws, `chat:room:${data.roomId}`);
            send(ws, 'chat:joined', { roomId: data.roomId });
            break;
        }
        case 'chat:leave': {
            await unsubscribe(ws, `chat:room:${data.roomId}`);
            break;
        }
        case 'chat:message': {
            console.log("inside websocket chat message event");
            const channel = `chat:room:${data.roomId}`;
            if (!ws.channels.has(channel)) {
                console.log("returning error: Join room first");
                return send(ws, 'error', { message: 'Join room first' });
            }
            const clientTempId = data.clientTempId;
            const serverTimeUTC = new Date().toISOString();
            const messageType = data.messageType ?? 'text';
            const text = data.text ?? null;
            const fileKey = data.fileKey ?? null;
            const fileName = data.fileName ?? null;
            const fileType = data.fileType ?? null;
            const fileSize = data.fileSize ?? null;
            const r = await db_1.default.query(`INSERT INTO chat_messages (
                    room_id,
                    sender_id,
                    message,
                    message_type,
                    file_key,
                    file_name,
                    file_type,
                    file_size,
                    sent_at
                )
                VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9)
                RETURNING id`, [
                data.roomId,
                uid,
                text,
                messageType,
                fileKey,
                fileName,
                fileType,
                fileSize,
                serverTimeUTC
            ]);
            console.log("inserted room message");
            await db_1.default.query(`UPDATE chat_rooms SET updated_at = $1 WHERE id = $2`, [serverTimeUTC, data.roomId]);
            await redis_1.publisher.del(`chat:history:${data.roomId}`);
            const payloadData = {
                messageId: r.rows[0].id,
                clientTempId,
                roomId: data.roomId,
                senderId: uid,
                text,
                messageType,
                fileKey,
                fileName,
                fileType,
                fileSize,
                sentAt: serverTimeUTC,
            };
            await redis_1.publisher.publish(channel, JSON.stringify({
                type: 'chat:message',
                data: payloadData
            }));
            const roomRes = await db_1.default.query(`SELECT patient_id, doctor_id FROM chat_rooms WHERE id = $1`, [data.roomId]);
            if (roomRes.rows.length > 0) {
                const room = roomRes.rows[0];
                const otherUserId = room.patient_id === uid ? room.doctor_id : room.patient_id;
                if (data.saveAsReport === true && fileKey) {
                    if (ws.role !== 'doctor') {
                        return send(ws, 'error', { message: 'Only doctors can save files as reports' });
                    }
                    const patientId = room.patient_id;
                    const doctorId = room.doctor_id;
                    if (doctorId !== uid) {
                        return send(ws, 'error', { message: 'Access denied' });
                    }
                    await db_1.default.query(`INSERT INTO medical_reports (
                        patient_id,
                        uploaded_by,
                        chat_room_id,
                        file_key,
                        file_name,
                        file_type,
                        file_size
                    )
                    VALUES ($1, $2, $3, $4, $5, $6, $7)`, [
                        patientId,
                        uid,
                        data.roomId,
                        fileKey,
                        fileName ?? 'file',
                        fileType,
                        fileSize
                    ]);
                }
                await sendToUser(otherUserId, 'chat:message', payloadData);
                if (!isUserOnline(otherUserId)) {
                    await (0, notification_service_1.sendPushNotificationToUser)({
                        userId: otherUserId,
                        title: 'New message',
                        body: messageType === 'text'
                            ? (text ?? 'New message')
                            : `Sent ${fileName ?? 'a file'}`,
                        dataOnly: true,
                        data: {
                            type: 'chat_message',
                            roomId: String(data.roomId),
                            senderId: String(uid),
                            messageId: String(r.rows[0].id),
                        }
                    });
                }
            }
            break;
        }
        case 'chat:read': {
            await db_1.default.query(`UPDATE chat_messages SET is_read = true WHERE id = $1 AND room_id = $2`, [data.messageId, data.roomId]);
            await redis_1.publisher.publish(`chat:room:${data.roomId}`, JSON.stringify({
                type: 'chat:read',
                data: { messageId: data.messageId },
            }));
            break;
        }
        default:
            send(ws, 'error', { message: `Unknown type: ${type}` });
    }
}
function initChatWebSocket(server) {
    const wss = new ws_1.WebSocketServer({ noServer: true });
    redis_1.subscriber.on('message', (channel, payload) => {
        channelRefs.get(channel)?.forEach(ws => {
            if (ws.readyState === ws_1.WebSocket.OPEN)
                ws.send(payload);
        });
    });
    server.on('upgrade', (req, socket, head) => {
        try {
            const url = new url_1.URL(req.url ?? '/', `http://${req.headers.host}`);
            if (url.pathname !== '/chat')
                return;
            const token = url.searchParams.get('token');
            if (!token) {
                socket.write('HTTP/1.1 401 Unauthorized\r\n\r\n');
                return socket.destroy();
            }
            const payload = jsonwebtoken_1.default.verify(token, process.env.JWT_SECRET);
            wss.handleUpgrade(req, socket, head, ws => {
                const s = ws;
                s.userId = payload.id;
                s.role = payload.role;
                s.channels = new Set();
                s.isAlive = true;
                wss.emit('connection', s);
            });
        }
        catch {
            socket.write('HTTP/1.1 401 Unauthorized\r\n\r\n');
            socket.destroy();
        }
    });
    wss.on('connection', async (ws) => {
        const uid = ws.userId;
        if (!userSockets.has(uid))
            userSockets.set(uid, new Set());
        userSockets.get(uid).add(ws);
        await subscribe(ws, `user:${uid}`);
        send(ws, 'connected', { userId: uid });
        ws.on('pong', () => { ws.isAlive = true; });
        ws.on('message', async (raw) => {
            try {
                const { type, data = {} } = JSON.parse(raw.toString());
                await handleMessage(ws, type, data);
            }
            catch (err) {
                console.error('handler error:', err);
                send(ws, 'error', { message: 'Invalid message' });
            }
        });
        ws.on('close', async () => {
            for (const ch of [...ws.channels])
                await unsubscribe(ws, ch);
            userSockets.get(uid)?.delete(ws);
            if (userSockets.get(uid)?.size === 0)
                userSockets.delete(uid);
        });
    });
    setInterval(() => {
        wss.clients.forEach(client => {
            const ws = client;
            if (!ws.isAlive)
                return ws.terminate();
            ws.isAlive = false;
            ws.ping();
        });
    }, 30000);
    console.log('WebSocket ready at /ws');
}
