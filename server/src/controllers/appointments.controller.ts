import { Request, Response } from 'express'
import db from '../config/db'
import { sendToUser, isUserOnline } from '../websocket/chat.ws'

export const bookAppointment = async (req: Request, res: Response) => {
    console.log("Inside bookAppointment controller")
    const patientId = (req as any).user.id;
    const { slotId } = req.body;

    if (!slotId) return res.status(400).json({ message: 'slotId required' });

    const client = await db.connect();
    try {
        await client.query('BEGIN');

        const slotRes = await client.query(
            `UPDATE appointment_slots
                SET status = 'booked'
              WHERE id = $1 AND status = 'available'
          RETURNING id, doctor_id, date, start_time, end_time, consultation_fee`,
            [slotId]
        );

        if (slotRes.rowCount === 0) {
            await client.query('ROLLBACK');
            return res.status(409).json({ message: 'Slot no longer available' });
        }
        const slot = slotRes.rows[0];
        const doctorId = slot.doctor_id;

        const docRes = await client.query(
            `SELECT consultation_type FROM doctor_professional WHERE user_id = $1`,
            [doctorId]
        );
        const consultType = docRes.rows[0]?.consultation_type ?? 'both';
        const apptType = consultType === 'offline' ? 'in_person' : 'online';

        const apptRes = await client.query(
            `INSERT INTO appointments (patient_id, doctor_id, type, slot_id)
             VALUES ($1, $2, $3, $4)
             RETURNING id, status, type, created_at`,
            [patientId, doctorId, apptType, slotId]
        );
        const appointment = apptRes.rows[0];

        await client.query(
            `UPDATE appointment_slots SET appointment_id = $1 WHERE id = $2`,
            [appointment.id, slotId]
        );

        const roomRes = await client.query(
            `INSERT INTO chat_rooms (appointment_id, patient_id, doctor_id)
             VALUES ($1, $2, $3) 
             ON CONFLICT (patient_id, doctor_id) 
             DO UPDATE SET appointment_id = EXCLUDED.appointment_id 
             RETURNING id`,
            [appointment.id, patientId, doctorId]
        );
        const roomId = roomRes.rows[0].id;

        const patientRes = await client.query(
            `SELECT u.id, u.name, u.profile_photo_key AS profile_photo 
             FROM users u 
             WHERE u.id = $1`,
            [patientId]
        );
        
        const doctorSnap = await client.query(
            `SELECT u.id, u.name, u.profile_photo_key AS profile_photo, pro.speciality
             FROM users u
             LEFT JOIN doctor_professional pro ON pro.user_id = u.id
             WHERE u.id = $1`,
            [doctorId]
        );

        await client.query('COMMIT');

        try {
            await sendToUser(doctorId, 'appointment:new', {
                appointment: {
                    id: appointment.id,
                    scheduledAt: slot.start_time, 
                    status: appointment.status,
                    type: appointment.type,
                    fee: slot.consultation_fee,
                },
                patient: patientRes.rows[0],
                roomId,
            });
        } catch (wsErr) {
            console.error('[bookAppointment] WS push failed:', wsErr);
        }

        res.status(201).json({
            appointment: { 
                ...appointment, 
                start_time: slot.start_time, 
                date: slot.date,
                fee: slot.consultation_fee 
            },
            doctor: doctorSnap.rows[0],
            roomId,
        });

    } catch (err) {
        await client.query('ROLLBACK');
        console.error('Book appointment error:', err);
        res.status(500).json({ message: 'Server error' });
    } finally {
        client.release();
    }
};

export const getPatientAppointments = async (req: Request, res: Response) => {
    console.log("Inside getPatientAppointments controller");
    const patientId = (req as any).user.id;
    
    const lastSync = (req.query.since as string) || '1970-01-01T00:00:00Z';

    try {
        const result = await db.query(
            `SELECT 
                u.name AS display_name,
                u.profile_photo_key AS profile_photo,
                a.id AS appointment_id,
                a.doctor_id, 
                a.status, 
                a.type, 
                dp.speciality,
                s.start_time,
                s.end_time,
                s.date,
                cr.id AS room_id,
                a.updated_at 
            FROM appointments AS a 
            JOIN users AS u ON a.doctor_id = u.id
            LEFT JOIN doctor_professional AS dp ON u.id = dp.user_id
            LEFT JOIN appointment_slots AS s ON a.slot_id = s.id
            LEFT JOIN chat_rooms AS cr ON a.id = cr.appointment_id
            WHERE a.patient_id = $1 AND a.updated_at > $2
            ORDER BY a.updated_at ASC`, 
            [patientId, lastSync]
        );

        if(result.rowCount === 0){
            return res.json({ appointments: [] });
        }
        res.json({ appointments: result.rows });
    }
    catch(error){
        console.error('Get patient appointments error:', error);
        res.status(500).json({ message: 'Server error' });
    }
};

export const getDoctorAppointments = async (req: Request, res: Response) => {
    console.log("Inside getDoctorAppointments controller");
    const doctorId = (req as any).user.id;
    
    const lastSync = (req.query.since as string) || '1970-01-01T00:00:00Z';

    try {
        const result = await db.query(
            `SELECT 
                u.name AS display_name, 
                u.profile_photo_key AS profile_photo,
                a.id AS appointment_id,
                a.patient_id, 
                a.status, 
                a.type, 
                s.start_time,
                s.end_time,
                s.date,
                cr.id AS room_id,
                a.updated_at
            FROM appointments AS a 
            JOIN users AS u ON a.patient_id = u.id
            LEFT JOIN appointment_slots AS s ON a.slot_id = s.id
            LEFT JOIN chat_rooms AS cr ON a.id = cr.appointment_id
            WHERE a.doctor_id = $1 AND a.updated_at > $2
            ORDER BY a.updated_at ASC`,
            [doctorId, lastSync]
        );

        if(result.rowCount === 0) return res.json({ appointments: [] });
        res.json({ appointments: result.rows });
    } catch(error) {
        console.error('Get doctor appointments error:', error);
        res.status(500).json({ message: 'Server error' });
    }
};