import { Request, Response } from 'express'
import db from '../config/db'

export const submitDoctorRating = async (req: Request, res: Response) => {
    try {
        const patientId = (req as any).user.id
        const role = (req as any).user.role
        const { appointmentId, rating, comment } = req.body

        if (role !== 'patient') {
            return res.status(403).json({ message: 'Only patients can rate doctors' })
        }

        if (!appointmentId || !rating || rating < 1 || rating > 5) {
            return res.status(400).json({
                message: 'appointmentId and rating between 1 and 5 are required'
            })
        }

        const appointmentRes = await db.query(
            `SELECT
                a.id,
                a.patient_id,
                a.doctor_id,
                a.status,
                s.date,
                s.end_time,
                (
                    s.date < CURRENT_DATE
                    OR (s.date = CURRENT_DATE AND s.end_time <= CURRENT_TIME)
                ) AS is_past
             FROM appointments a
             LEFT JOIN appointment_slots s ON s.id = a.slot_id
             WHERE a.id = $1 AND a.patient_id = $2`,
            [appointmentId, patientId]
        )

        if (appointmentRes.rowCount === 0) {
            return res.status(404).json({ message: 'Appointment not found' })
        }

        const appointment = appointmentRes.rows[0]

        if (appointment.status !== 'completed' && !appointment.is_past) {
            return res.status(400).json({
                message: 'You can rate only after appointment is completed or past'
            })
        }

        const result = await db.query(
            `INSERT INTO doctor_ratings (
                doctor_id,
                patient_id,
                appointment_id,
                rating,
                comment
            )
             VALUES ($1, $2, $3, $4, $5)
             ON CONFLICT (appointment_id)
             DO UPDATE SET
                rating = EXCLUDED.rating,
                comment = EXCLUDED.comment,
                updated_at = NOW()
             RETURNING id, doctor_id, patient_id, appointment_id, rating, comment, created_at, updated_at`,
            [
                appointment.doctor_id,
                patientId,
                appointmentId,
                rating,
                comment ?? null
            ]
        )

        return res.status(200).json({
            message: 'Rating saved successfully',
            rating: result.rows[0]
        })
    } catch (error) {
        console.error('submitDoctorRating error:', error)
        return res.status(500).json({ message: 'Failed to save rating' })
    }
}

export const getAppointmentRating = async (req: Request, res: Response) => {
    try {
        const userId = (req as any).user.id
        const role = (req as any).user.role
        const { appointmentId } = req.params

        const appointmentRes = await db.query(
            `SELECT id, patient_id, doctor_id
             FROM appointments
             WHERE id = $1`,
            [appointmentId]
        )

        if (appointmentRes.rowCount === 0) {
            return res.status(404).json({ message: 'Appointment not found' })
        }

        const appointment = appointmentRes.rows[0]

        const canView =
            (role === 'patient' && appointment.patient_id === userId) ||
            (role === 'doctor' && appointment.doctor_id === userId)

        if (!canView) {
            return res.status(403).json({ message: 'Access denied' })
        }

        const ratingRes = await db.query(
            `SELECT id, rating, comment, created_at AS "createdAt", updated_at AS "updatedAt"
             FROM doctor_ratings
             WHERE appointment_id = $1`,
            [appointmentId]
        )

        return res.status(200).json({
            rating: ratingRes.rows[0] ?? null
        })
    } catch (error) {
        console.error('getAppointmentRating error:', error)
        return res.status(500).json({ message: 'Failed to fetch rating' })
    }
}

export const getDoctorRatingSummary = async (req: Request, res: Response) => {
    try {
        const { doctorId } = req.params

        const result = await db.query(
            `SELECT
                COALESCE(ROUND(AVG(rating)::numeric, 1), 0) AS average,
                COUNT(*)::int AS count
             FROM doctor_ratings
             WHERE doctor_id = $1`,
            [doctorId]
        )

        return res.status(200).json({
            average: Number(result.rows[0].average),
            count: result.rows[0].count
        })
    } catch (error) {
        console.error('getDoctorRatingSummary error:', error)
        return res.status(500).json({ message: 'Failed to fetch rating summary' })
    }
}
