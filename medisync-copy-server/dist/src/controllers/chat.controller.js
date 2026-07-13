"use strict";
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.getRoomMessages = exports.getInbox = exports.getOrCreateChatRoom = exports.getRoomMetadata = void 0;
const db_1 = __importDefault(require("../config/db"));
const getRoomMetadata = async (req, res) => {
    const userId = req.user.id;
    const { roomId } = req.params;
    try {
        const result = await db_1.default.query(`SELECT 
                CASE 
                    WHEN r.patient_id = $1 THEN u_doc.name 
                    ELSE u_pat.name 
                END AS display_name,
                CASE 
                    WHEN r.patient_id = $1 THEN u_doc.profile_photo_key 
                    ELSE u_pat.profile_photo_key 
                END AS profile_photo,
                CASE 
                    WHEN r.patient_id = $1 THEN 'Doctor' 
                    ELSE 'Patient' 
                END AS other_role
            FROM chat_rooms r
            LEFT JOIN users u_doc ON r.doctor_id = u_doc.id
            LEFT JOIN users u_pat ON r.patient_id = u_pat.id
            WHERE r.id = $2`, [userId, roomId]);
        if (result.rowCount === 0)
            return res.status(404).json({ message: "Room not found" });
        res.json(result.rows[0]);
    }
    catch (error) {
        res.status(500).json({ message: "Server error" });
    }
};
exports.getRoomMetadata = getRoomMetadata;
const getOrCreateChatRoom = async (req, res) => {
    const requesterId = req.user.id;
    const requesterRole = req.user.role;
    const { targetUserId } = req.body;
    if (!targetUserId) {
        return res.status(400).json({ message: 'targetUserId is required' });
    }
    let patientId;
    let doctorId;
    if (requesterRole === 'patient') {
        patientId = requesterId;
        doctorId = targetUserId;
    }
    else if (requesterRole === 'doctor') {
        patientId = targetUserId;
        doctorId = requesterId;
    }
    else {
        return res.status(403).json({ message: 'Invalid user role' });
    }
    try {
        const existingRoomRes = await db_1.default.query(`SELECT id FROM chat_rooms WHERE patient_id = $1 AND doctor_id = $2`, [patientId, doctorId]);
        if (existingRoomRes.rows.length > 0) {
            return res.status(200).json({
                roomId: existingRoomRes.rows[0].id,
                isNew: false
            });
        }
        const newRoomRes = await db_1.default.query(`INSERT INTO chat_rooms (patient_id, doctor_id) VALUES ($1, $2) RETURNING id`, [patientId, doctorId]);
        return res.status(201).json({
            roomId: newRoomRes.rows[0].id,
            isNew: true
        });
    }
    catch (err) {
        if (err.code === '23505') {
            const recoveryRes = await db_1.default.query(`SELECT id FROM chat_rooms WHERE patient_id = $1 AND doctor_id = $2`, [patientId, doctorId]);
            if (recoveryRes.rows.length > 0) {
                return res.status(200).json({
                    roomId: recoveryRes.rows[0].id,
                    isNew: false
                });
            }
        }
        return res.status(500).json({ message: 'Server error' });
    }
};
exports.getOrCreateChatRoom = getOrCreateChatRoom;
const getInbox = async (req, res) => {
    const userId = req.user.id;
    const userRole = req.user.role;
    const lastSync = req.query.since || '1970-01-01T00:00:00Z';
    try {
        let query = '';
        if (userRole === 'patient') {
            query = `
                SELECT 
                    cr.id AS room_id, 
                    u.id AS other_user_id, 
                    u.name AS display_name, 
                    u.profile_photo_key AS profile_photo, 
                    pro.speciality,
                    m.message AS last_message,
                    m.sent_at AS last_message_time,
                    -- Count unread messages sent by the doctor (not the current user)
                    (SELECT COUNT(*)::int 
                     FROM chat_messages cm 
                     WHERE cm.room_id = cr.id AND cm.sender_id != $1 AND cm.is_read = false) AS unread_count,
                    cr.updated_at
                FROM chat_rooms cr
                JOIN users u ON cr.doctor_id = u.id
                LEFT JOIN doctor_professional pro ON pro.user_id = u.id
                -- Grab the single most recent message for this room
                LEFT JOIN LATERAL (
                    SELECT message, sent_at 
                    FROM chat_messages 
                    WHERE room_id = cr.id 
                    ORDER BY sent_at DESC 
                    LIMIT 1
                ) m ON true
                WHERE cr.patient_id = $1 AND cr.updated_at > $2
                ORDER BY cr.updated_at ASC
            `;
        }
        else if (userRole === 'doctor') {
            query = `
                SELECT 
                    cr.id AS room_id, 
                    u.id AS other_user_id, 
                    u.name AS display_name, 
                    u.profile_photo_key AS profile_photo,
                    NULL AS speciality,
                    m.message AS last_message,
                    m.sent_at AS last_message_time,
                    -- Count unread messages sent by the patient (not the current user)
                    (SELECT COUNT(*)::int 
                     FROM chat_messages cm 
                     WHERE cm.room_id = cr.id AND cm.sender_id != $1 AND cm.is_read = false) AS unread_count,
                    cr.updated_at
                FROM chat_rooms cr
                JOIN users u ON cr.patient_id = u.id
                -- Grab the single most recent message for this room
                LEFT JOIN LATERAL (
                    SELECT message, sent_at 
                    FROM chat_messages 
                    WHERE room_id = cr.id 
                    ORDER BY sent_at DESC 
                    LIMIT 1
                ) m ON true
                WHERE cr.doctor_id = $1 AND cr.updated_at > $2
                ORDER BY cr.updated_at ASC
            `;
        }
        else {
            return res.status(403).json({ message: 'Invalid user role' });
        }
        const inboxRes = await db_1.default.query(query, [userId, lastSync]);
        return res.status(200).json({
            chats: inboxRes.rows
        });
    }
    catch (err) {
        console.error('Get inbox error:', err);
        return res.status(500).json({ message: 'Server error' });
    }
};
exports.getInbox = getInbox;
const getRoomMessages = async (req, res) => {
    const { roomId } = req.params;
    const lastSync = req.query.since || '1970-01-01T00:00:00Z';
    console.log("inside getroom message id : ", roomId);
    if (!roomId) {
        return res.status(400).json({ message: 'roomId is required' });
    }
    try {
        const messagesRes = await db_1.default.query(`SELECT 
                id, 
                room_id AS "roomId",
                sender_id AS "senderId", 
                message AS "text",
                message_type AS "messageType",
                file_key AS "fileKey",
                file_name AS "fileName",
                file_type AS "fileType",
                file_size AS "fileSize",
                is_read AS "isRead",
                sent_at AS "sentAt"
            FROM chat_messages 
            WHERE room_id = $1 AND sent_at > $2
            ORDER BY sent_at ASC`, [roomId, lastSync]);
        return res.status(200).json(messagesRes.rows);
    }
    catch (err) {
        console.error(err);
        return res.status(500).json({ message: 'Server error' });
    }
};
exports.getRoomMessages = getRoomMessages;
