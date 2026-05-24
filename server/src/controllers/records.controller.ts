import { Request, Response } from 'express'
import db from '../config/db'

export const getPatientRecords = async (req: Request, res: Response) => {
    try {
        const userId = (req as any).user.id
        const role = (req as any).user.role

        if (role !== 'patient') {
            return res.status(403).json({ message: 'Only patients can view patient records' })
        }

        const result = await db.query(
            `SELECT
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
             ORDER BY mr.created_at DESC`,
            [userId]
        )

        return res.status(200).json({
            records: result.rows
        })
    } catch (error) {
        console.error('getPatientRecords error:', error)
        return res.status(500).json({ message: 'Failed to fetch patient records' })
    }
}