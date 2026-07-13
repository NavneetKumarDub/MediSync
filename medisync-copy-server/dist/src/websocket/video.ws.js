"use strict";
var __createBinding = (this && this.__createBinding) || (Object.create ? (function(o, m, k, k2) {
    if (k2 === undefined) k2 = k;
    var desc = Object.getOwnPropertyDescriptor(m, k);
    if (!desc || ("get" in desc ? !m.__esModule : desc.writable || desc.configurable)) {
      desc = { enumerable: true, get: function() { return m[k]; } };
    }
    Object.defineProperty(o, k2, desc);
}) : (function(o, m, k, k2) {
    if (k2 === undefined) k2 = k;
    o[k2] = m[k];
}));
var __setModuleDefault = (this && this.__setModuleDefault) || (Object.create ? (function(o, v) {
    Object.defineProperty(o, "default", { enumerable: true, value: v });
}) : function(o, v) {
    o["default"] = v;
});
var __importStar = (this && this.__importStar) || (function () {
    var ownKeys = function(o) {
        ownKeys = Object.getOwnPropertyNames || function (o) {
            var ar = [];
            for (var k in o) if (Object.prototype.hasOwnProperty.call(o, k)) ar[ar.length] = k;
            return ar;
        };
        return ownKeys(o);
    };
    return function (mod) {
        if (mod && mod.__esModule) return mod;
        var result = {};
        if (mod != null) for (var k = ownKeys(mod), i = 0; i < k.length; i++) if (k[i] !== "default") __createBinding(result, mod, k[i]);
        __setModuleDefault(result, mod);
        return result;
    };
})();
Object.defineProperty(exports, "__esModule", { value: true });
exports.initVideoWebSocket = void 0;
const ws_1 = require("ws");
const url_1 = require("url");
const crypto = __importStar(require("crypto"));
const socketToWsMap = new Map();
const roomToSocketsMap = new Map();
const safeSend = (ws, payload) => {
    if (ws.readyState === ws_1.WebSocket.OPEN) {
        ws.send(JSON.stringify(payload));
    }
};
const broadcastToRoom = (roomId, senderSocketId, payload) => {
    const socketsInRoom = roomToSocketsMap.get(roomId);
    if (!socketsInRoom)
        return;
    socketsInRoom.forEach((socketId) => {
        if (socketId === senderSocketId)
            return;
        const ws = socketToWsMap.get(socketId);
        if (ws)
            safeSend(ws, payload);
    });
};
const leaveRoom = (socketId, roomId) => {
    const socketsInRoom = roomToSocketsMap.get(roomId);
    if (!socketsInRoom)
        return;
    socketsInRoom.delete(socketId);
    broadcastToRoom(roomId, socketId, {
        type: 'user-left',
        roomId,
        id: socketId,
    });
    if (socketsInRoom.size === 0) {
        roomToSocketsMap.delete(roomId);
        console.log(`[Video] Room ${roomId} deleted (empty).`);
    }
};
const handleVideoSignaling = (ws) => {
    const socketId = crypto.randomUUID();
    socketToWsMap.set(socketId, ws);
    const joinedRooms = new Set();
    console.log(`[Video] Connected: ${socketId}`);
    ws.on('message', (raw) => {
        try {
            const signal = JSON.parse(raw.toString());
            const { type, roomId } = signal;
            switch (type) {
                case 'join-room': {
                    if (!roomId)
                        break;
                    if (!roomToSocketsMap.has(roomId)) {
                        roomToSocketsMap.set(roomId, new Set());
                    }
                    roomToSocketsMap.get(roomId).add(socketId);
                    joinedRooms.add(roomId);
                    console.log(`[Video] ${socketId} joined room ${roomId}`);
                    broadcastToRoom(roomId, socketId, {
                        type: 'user-joined',
                        roomId,
                        id: socketId,
                    });
                    safeSend(ws, {
                        type: 'room-joined',
                        roomId,
                        id: socketId,
                    });
                    break;
                }
                case 'leave-room': {
                    if (!roomId)
                        break;
                    leaveRoom(socketId, roomId);
                    joinedRooms.delete(roomId);
                    console.log(`[Video] ${socketId} left room ${roomId}`);
                    break;
                }
                case 'offer': {
                    const payload = { type: 'offer', roomId, id: socketId, sdp: signal.sdp };
                    if (signal.targetId) {
                        const targetWs = socketToWsMap.get(signal.targetId);
                        if (!targetWs) {
                            console.warn(`[Video] Target not found for offer: ${signal.targetId}`);
                            break;
                        }
                        safeSend(targetWs, payload);
                    }
                    else {
                        broadcastToRoom(roomId, socketId, payload);
                    }
                    break;
                }
                case 'answer': {
                    const payload = { type: 'answer', roomId, id: socketId, sdp: signal.sdp };
                    if (signal.targetId) {
                        const targetWs = socketToWsMap.get(signal.targetId);
                        if (!targetWs) {
                            console.warn(`[Video] Target not found for answer: ${signal.targetId}`);
                            break;
                        }
                        safeSend(targetWs, payload);
                    }
                    else {
                        broadcastToRoom(roomId, socketId, payload);
                    }
                    break;
                }
                case 'renegotiate': {
                    const payload = { type: 'renegotiate', roomId, id: socketId, sdp: signal.sdp };
                    if (signal.targetId) {
                        const targetWs = socketToWsMap.get(signal.targetId);
                        if (!targetWs) {
                            console.warn(`[Video] Target not found for renegotiate: ${signal.targetId}`);
                            break;
                        }
                        safeSend(targetWs, payload);
                    }
                    else {
                        broadcastToRoom(roomId, socketId, payload);
                    }
                    break;
                }
                case 'ice-candidate': {
                    const payload = { type: 'ice-candidate', roomId, id: socketId, candidate: signal.candidate };
                    if (signal.targetId) {
                        const targetWs = socketToWsMap.get(signal.targetId);
                        if (!targetWs) {
                            console.warn(`[Video] Target not found for ice-candidate: ${signal.targetId}`);
                            break;
                        }
                        safeSend(targetWs, payload);
                    }
                    else {
                        broadcastToRoom(roomId, socketId, payload);
                    }
                    break;
                }
                default:
                    console.warn(`[Video] Unknown signal type: ${type}`);
            }
        }
        catch (err) {
            console.error('[Video] Failed to process message:', err);
        }
    });
    ws.on('close', () => {
        console.log(`[Video] Disconnected: ${socketId}`);
        joinedRooms.forEach((roomId) => leaveRoom(socketId, roomId));
        joinedRooms.clear();
        socketToWsMap.delete(socketId);
    });
    ws.on('error', (err) => {
        console.error(`[Video] Socket error on ${socketId}:`, err);
    });
};
const initVideoWebSocket = (server) => {
    const wss = new ws_1.WebSocketServer({ noServer: true });
    server.on('upgrade', (req, socket, head) => {
        const url = new url_1.URL(req.url ?? '/', `http://${req.headers.host}`);
        if (url.pathname !== '/video')
            return;
        wss.handleUpgrade(req, socket, head, (ws) => {
            wss.emit('connection', ws);
        });
    });
    wss.on('connection', (ws) => {
        handleVideoSignaling(ws);
    });
    console.log('Video WebSocket ready at /video');
};
exports.initVideoWebSocket = initVideoWebSocket;
