"use strict";
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.getChatFileViewUrl = exports.getChatFileUploadUrl = exports.deleteProfilePhoto = exports.getProfilePhotoUrl = exports.confirmProfilePhotoUpload = exports.getUploadUrl = void 0;
const db_1 = __importDefault(require("../config/db"));
const minio_1 = require("../config/minio");
const getUploadUrl = async (req, res) => {
    try {
        const { userId, fileName, fileType } = req.body;
        if (!userId || !fileName || !fileType) {
            return res.status(400).json({ message: 'userId, fileName and fileType are required' });
        }
        const timestamp = Date.now();
        const extension = fileName.split('.').pop();
        const key = `users/${userId}/avatar_${timestamp}.${extension}`;
        const uploadUrl = await (0, minio_1.getPresignedUploadUrl)(minio_1.BUCKETS.PROFILE_PHOTOS, key, 900);
        return res.status(200).json({
            uploadUrl,
            key
        });
    }
    catch (error) {
        console.error('Error generating presigned upload URL:', error);
        return res.status(500).json({ message: 'Failed to generate upload URL' });
    }
};
exports.getUploadUrl = getUploadUrl;
const confirmProfilePhotoUpload = async (req, res) => {
    try {
        const { userId, key } = req.body;
        if (!userId || !key) {
            return res.status(400).json({ message: 'userId and key are required' });
        }
        await db_1.default.query('UPDATE users SET profile_photo_key = $1 WHERE id = $2', [key, userId]);
        return res.status(200).json({ message: 'Profile photo updated successfully', key });
    }
    catch (error) {
        console.error('Error confirming upload:', error);
        return res.status(500).json({ message: 'Failed to confirm upload' });
    }
};
exports.confirmProfilePhotoUpload = confirmProfilePhotoUpload;
const getProfilePhotoUrl = async (req, res) => {
    try {
        const { userId } = req.params;
        const result = await db_1.default.query('SELECT profile_photo_key FROM users WHERE id = $1', [userId]);
        if (!result.rows[0] || !result.rows[0].profile_photo_key) {
            return res.status(404).json({ message: 'No profile photo found' });
        }
        const key = result.rows[0].profile_photo_key;
        const viewUrl = await (0, minio_1.getPresignedDownloadUrl)(minio_1.BUCKETS.PROFILE_PHOTOS, key, 3600);
        return res.status(200).json({ viewUrl });
    }
    catch (error) {
        console.error('Error generating view URL:', error);
        return res.status(500).json({ message: 'Failed to generate view URL' });
    }
};
exports.getProfilePhotoUrl = getProfilePhotoUrl;
const deleteProfilePhoto = async (req, res) => {
    try {
        const { userId } = req.params;
        const result = await db_1.default.query('SELECT profile_photo_key FROM users WHERE id = $1', [userId]);
        const key = result.rows[0]?.profile_photo_key;
        if (key) {
            await (0, minio_1.deleteObject)(minio_1.BUCKETS.PROFILE_PHOTOS, key);
        }
        await db_1.default.query('UPDATE users SET profile_photo_key = NULL WHERE id = $1', [userId]);
        return res.status(200).json({ message: 'Profile photo deleted' });
    }
    catch (error) {
        return res.status(500).json({ message: 'Failed to delete photo' });
    }
};
exports.deleteProfilePhoto = deleteProfilePhoto;
const getChatFileUploadUrl = async (req, res) => {
    try {
        const userId = req.user.id;
        const { roomId, fileName, fileType } = req.body;
        if (!roomId || !fileName || !fileType) {
            return res.status(400).json({
                message: 'roomId, fileName and fileType are required'
            });
        }
        const roomRes = await db_1.default.query(`SELECT id FROM chat_rooms
             WHERE id = $1 AND (patient_id = $2 OR doctor_id = $2)`, [roomId, userId]);
        if (roomRes.rowCount === 0) {
            return res.status(403).json({ message: 'Access denied' });
        }
        const timestamp = Date.now();
        const safeName = fileName.replace(/[^a-zA-Z0-9._-]/g, '_');
        const key = `chat/${roomId}/${userId}_${timestamp}_${safeName}`;
        const uploadUrl = await (0, minio_1.getPresignedUploadUrl)(minio_1.BUCKETS.MEDICAL_RECORDS, key, 900);
        return res.status(200).json({
            uploadUrl,
            key
        });
    }
    catch (error) {
        console.error('getChatFileUploadUrl error:', error);
        return res.status(500).json({ message: 'Failed to generate upload URL' });
    }
};
exports.getChatFileUploadUrl = getChatFileUploadUrl;
const getChatFileViewUrl = async (req, res) => {
    try {
        const userId = req.user.id;
        const key = req.query.key;
        if (!key) {
            return res.status(400).json({ message: 'key is required' });
        }
        const accessRes = await db_1.default.query(`SELECT cm.id
             FROM chat_messages cm
             JOIN chat_rooms cr ON cr.id = cm.room_id
             WHERE cm.file_key = $1
               AND (cr.patient_id = $2 OR cr.doctor_id = $2)
             LIMIT 1`, [key, userId]);
        const reportAccessRes = await db_1.default.query(`SELECT id
             FROM medical_reports
             WHERE file_key = $1
               AND (patient_id = $2 OR uploaded_by = $2)
             LIMIT 1`, [key, userId]);
        if (accessRes.rowCount === 0 && reportAccessRes.rowCount === 0) {
            return res.status(403).json({ message: 'Access denied' });
        }
        const viewUrl = await (0, minio_1.getPresignedDownloadUrl)(minio_1.BUCKETS.MEDICAL_RECORDS, key, 300);
        return res.status(200).json({ viewUrl });
    }
    catch (error) {
        console.error('getChatFileViewUrl error:', error);
        return res.status(500).json({ message: 'Failed to generate file URL' });
    }
};
exports.getChatFileViewUrl = getChatFileViewUrl;
