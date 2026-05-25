import { Request, Response } from 'express'
import db from '../config/db'

// ── Get available dates (next 30 days that have slots) ──
export const getDoctorAvailableDates = async (req: Request, res: Response) => {
    const { doctorId } = req.params
    console.log("Inside get doctor available dates controller : ",doctorId)

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
    console.log("Inside get doctor slots controller")
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
                consultation_type,
                slot_duration_minutes,
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

export const addRegularSlot = async (req: Request, res: Response) => {
    console.log("Inside add regular slot controller")
    try {
        const user_id = (req as any).user.id;

        const {
            day_of_week,
            start_time,
            end_time,
            slot_duration_minutes,
            consultation_fee,
            
        } = req.body;
        let { consultation_type } = req.body;

        if (
            !day_of_week ||
            !start_time ||
            !end_time ||
            !slot_duration_minutes ||
            !consultation_fee
        ) {
            return res.status(400).json({
                success: false,
                message: "All fields are required",
            });
        }
        if(!consultation_type){
            consultation_type = 'Offline';
        }
        const conflictCheck = await db.query(
            `SELECT id FROM doctor_availability
             WHERE user_id = $1
               AND day_of_week = $2
               AND is_active = TRUE
               AND (
                   (start_time < $4 AND end_time > $3)
               )`,
            [user_id, day_of_week, start_time, end_time]
        );

        if (conflictCheck.rows.length > 0) {
            return res.status(409).json({
                success: false,
                message: "This slot overlaps with an existing slot on the same day",
            });
        }

        const result = await db.query(
            `INSERT INTO doctor_availability 
                (user_id, day_of_week, start_time, end_time, slot_duration_minutes, consultation_fee, consultation_type, is_active)
             VALUES ($1, $2, $3, $4, $5, $6, $7, TRUE)
             RETURNING *`,
            [
                user_id,
                day_of_week,
                start_time,
                end_time,
                slot_duration_minutes,
                consultation_fee,
                consultation_type,
            ]
        );

        return res.status(201).json({
            success: true,
            message: "Slot added successfully",
            slot: result.rows[0],
        });
    } catch (error) {
        console.error("addSlot error:", error);
        return res.status(500).json({
            success: false,
            message: "Internal server error",
        });
    }
};


export const deleteRegularSlot = async (req: Request, res: Response) => {
    console.log("Inside delete regular slot controller");
    try {
        const user_id = (req as any).user.id;
        const { slotId } = req.params;

        const slot = await db.query(
            `SELECT id FROM doctor_availability
             WHERE id = $1 AND user_id = $2 AND is_active = TRUE`,
            [slotId, user_id]
        );

        if (slot.rows.length === 0) {
            return res.status(404).json({
                success: false,
                message: "Slot not found or already deleted",
            });
        }

        await db.query(
            `UPDATE doctor_availability
             SET is_active = FALSE
             WHERE id = $1 AND user_id = $2`,
            [slotId, user_id]
        );

        return res.status(200).json({
            success: true,
            message: "Slot deleted successfully",
        });
    } catch (error) {
        console.error("deleteSlot error:", error);
        return res.status(500).json({
            success: false,
            message: "Internal server error",
        });
    }
};


export const getRegularSlots = async (req: Request, res: Response) => {
    console.log("Inside get regular slots controller")
    try {
        const user_id = (req as any).user.id;
        const { day } = req.query; 

        let query: string;
        let params: any[];

        if (day) {
            query = `
                SELECT 
                    id,
                    day_of_week,
                    start_time,
                    end_time,
                    slot_duration_minutes,
                    consultation_fee,
                    consultation_type,
                    created_at
                FROM doctor_availability
                WHERE user_id = $1
                  AND day_of_week = $2
                  AND is_active = TRUE
                ORDER BY start_time ASC
            `;
            params = [user_id, day];
        } else {
            query = `
                SELECT 
                    id,
                    day_of_week,
                    start_time,
                    end_time,
                    slot_duration_minutes,
                    consultation_fee,
                    consultation_type,
                    created_at
                FROM doctor_availability
                WHERE user_id = $1
                  AND is_active = TRUE
                ORDER BY 
                    CASE day_of_week
                        WHEN 'Mon' THEN 1
                        WHEN 'Tue' THEN 2
                        WHEN 'Wed' THEN 3
                        WHEN 'Thu' THEN 4
                        WHEN 'Fri' THEN 5
                        WHEN 'Sat' THEN 6
                        WHEN 'Sun' THEN 7
                    END,
                    start_time ASC
            `;
            params = [user_id];
        }

        const result = await db.query(query, params);

        return res.status(200).json({
            success: true,
            slots: result.rows,
        });
    } catch (error) {
        console.error("getSlots error:", error);
        return res.status(500).json({
            success: false,
            message: "Internal server error",
        });
    }
};





export const createCustomSlot = async (req: Request, res: Response) => {
    console.log("Inside create custom slot controller")
    try {
        const user_id = (req as any).user.id

        const {
            date,
            start_time,
            end_time,
            consultation_fee,
            consultation_type,
            slot_duration_minutes
        } = req.body

        if (!date || !start_time || !end_time || !consultation_fee || !slot_duration_minutes) {
            return res.status(400).json({
                success: false,
                message: 'All fields are required'
            })
        }

        const conflictCheck = await db.query(
            `SELECT id FROM appointment_slots
             WHERE doctor_id = $1
             AND date = $2
             AND is_active = TRUE
             AND (
                 start_time < $4 AND end_time > $3
             )`,
            [user_id, date, start_time, end_time]
        )

        if (conflictCheck.rows.length > 0) {
            return res.status(409).json({
                success: false,
                message: 'This slot overlaps with an existing slot on the same date'
            })
        }

        const result = await db.query(
            `INSERT INTO appointment_slots
             (doctor_id, date, start_time, end_time, consultation_fee, consultation_type, slot_duration_minutes, status, is_active)
             VALUES ($1, $2, $3, $4, $5, $6, $7, 'available', TRUE)
             RETURNING *`,
            [user_id, date, start_time, end_time, consultation_fee, consultation_type || 'Offline', slot_duration_minutes]
        )

        return res.status(201).json({
            success: true,
            message: 'Slot created successfully',
            slot: result.rows[0]
        })

    } catch (error) {
        console.error('createCustomSlot error:', error)
        return res.status(500).json({
            success: false,
            message: 'Internal server error'
        })
    }
}

export const deleteCustomSlot = async (req: Request, res: Response) => {
    console.log("Inside delete custom slot controller")
    try {
        const user_id = (req as any).user.id
        const { slotId } = req.params

        const slot = await db.query(
            `SELECT id FROM appointment_slots
             WHERE id = $1 AND doctor_id = $2 AND is_active = TRUE`,
            [slotId, user_id]
        )

        if (slot.rows.length === 0) {
            return res.status(404).json({
                success: false,
                message: 'Slot not found or already deleted'
            })
        }

        await db.query(
            `UPDATE appointment_slots
             SET is_active = FALSE
             WHERE id = $1 AND doctor_id = $2`,
            [slotId, user_id]
        )

        return res.status(200).json({
            success: true,
            message: 'Slot deleted successfully'
        })

    } catch (error) {
        console.error('deleteCustomSlot error:', error)
        return res.status(500).json({
            success: false,
            message: 'Internal server error'
        })
    }
}

export const getSlotsByDate = async (req: Request, res: Response) => {
    console.log("Inside get slots by date controller")
    try {
        const user_id = (req as any).user.id
        const { date } = req.query

        if (!date) {
            return res.status(400).json({
                success: false,
                message: 'date query param required'
            })
        }

        const result = await db.query(
            `SELECT
                id,
                TO_CHAR(date, 'YYYY-MM-DD') AS date,
                start_time,
                end_time,
                consultation_fee,
                consultation_type,
                slot_duration_minutes,
                status
             FROM appointment_slots
             WHERE doctor_id = $1
             AND date = $2
             AND is_active = TRUE
             ORDER BY start_time ASC`,
            [user_id, date]
        )

        return res.status(200).json({
            success: true,
            slots: result.rows
        })

    } catch (error) {
        console.error('getSlotsByDate error:', error)
        return res.status(500).json({
            success: false,
            message: 'Internal server error'
        })
    }
}

export const getDatesWithSlots = async (req: Request, res: Response) => {
    try {
        console.log("Inside get dates with slots controller")
        const user_id = (req as any).user.id
        const { month, year } = req.query

        if (!month || !year) {
            return res.status(400).json({
                success: false,
                message: 'month and year query params required'
            })
        }

        const result = await db.query(
            `SELECT DISTINCT TO_CHAR(date, 'YYYY-MM-DD') as date
             FROM appointment_slots
             WHERE doctor_id = $1
             AND is_active = TRUE
             AND EXTRACT(MONTH FROM date) = $2
             AND EXTRACT(YEAR FROM date) = $3
             ORDER BY date ASC`,
            [user_id, month, year]
        )

        return res.status(200).json({
            success: true,
            dates: result.rows.map((r: any) => r.date)
        })

    } catch (error) {
        console.error('getDatesWithSlots error:', error)
        return res.status(500).json({
            success: false,
            message: 'Internal server error'
        })
    }
}
