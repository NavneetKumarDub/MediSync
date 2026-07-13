"use strict";
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.getPatientRecords = void 0;
const db_1 = __importDefault(require("../config/db"));
const getPatientRecords = async (req, res) => {
    try {
        const userId = req.user.id;
        const role = req.user.role;
        if (role !== 'patient') {
            return res.status(403).json({ message: 'Only patients can view patient records' });
        }
        const result = await db_1.default.query(`SELECT
                mr.id,
                mr.file_key AS "fileKey",
                mr.file_name AS "fileName",
                mr.file_type AS "fileType",
                mr.file_size AS "fileSize",
                mr.created_at AS "createdAt",
                u.name AS "uploadedByName"
             FROM medical_reports mr
             JOIN users u ON u.id = mr.uploaded_by
             WHERE mr.patient_id = $1
             ORDER BY mr.created_at DESC`, [userId]);
        return res.status(200).json({
            records: result.rows
        });
    }
    catch (error) {
        console.error('getPatientRecords error:', error);
        return res.status(500).json({ message: 'Failed to fetch patient records' });
    }
};
exports.getPatientRecords = getPatientRecords;
