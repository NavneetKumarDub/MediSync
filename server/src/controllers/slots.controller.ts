import { Request, Response } from 'express'
import db from '../config/db'

// ── Get available dates (next 30 days that have slots) ──
export const getDoctorAvailableDates = async (req: Request, res: Response) => {
    const { doctorId } = req.params

    try {
        const result = await db.query(
            `SELECT DISTINCT TO_CHAR(date,'YYYY-MM-DD') as date
             FROM appointment_slots
             WHERE doctor_id = $1
             AND date >= CURRENT_DATE
             AND status = 'available'
             ORDER BY date`,
            [doctorId]
        )

        res.json({ dates: result.rows.map(r => r.date) })

    } catch (error) {
        console.error('Get available dates error:', error)
        res.status(500).json({ message: 'Server error' })
    }
}

// ── Get slots for a specific date ──────────────
export const getDoctorSlots = async (req: Request, res: Response) => {
    const { doctorId } = req.params
    const { date }     = req.query

    console.log("Inside get doctor slots " , doctorId,date)

    if (!date) {
        return res.status(400).json({ message: 'date query param required' })
    }

    try {
        const result = await db.query(
            `SELECT
                id,
                start_time,
                end_time,
                consultation_fee,
                status
             FROM appointment_slots
             WHERE doctor_id = $1
             AND date = $2
             ORDER BY start_time`,
            [doctorId, date]
        )

        res.json({ slots: result.rows })

    } catch (error) {
        console.error('Get slots error:', error)
        res.status(500).json({ message: 'Server error' })
    }
}

// ── Get doctor weekly availability ────────────
export const getDoctorAvailability = async (req: Request, res: Response) => {
    const { doctorId } = req.params
    try {
        const result = await db.query(
            `SELECT day_of_week, start_time, end_time, slot_duration_minutes
             FROM doctor_availability
             WHERE user_id = $1 AND is_active = true
             ORDER BY CASE day_of_week
                WHEN 'Monday'    THEN 1
                WHEN 'Tuesday'   THEN 2
                WHEN 'Wednesday' THEN 3
                WHEN 'Thursday'  THEN 4
                WHEN 'Friday'    THEN 5
                WHEN 'Saturday'  THEN 6
                WHEN 'Sunday'    THEN 7
             END`,
            [doctorId]
        )
        res.json({ availability: result.rows })
    } catch (error) {
        console.error('Get availability error:', error)
        res.status(500).json({ message: 'Server error' })
    }
}