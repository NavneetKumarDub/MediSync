import { Request, Response } from 'express'
import db from '../config/db'

export const updateDoctorPersonal = async (req: Request, res: Response) => {
    const { userId } = req.params
    const { email, gender, dob, marital_status, about } = req.body
    try {
        const existing = await db.query(
            'SELECT * FROM doctor_personal WHERE user_id = $1', [userId]
        )
        if (existing.rows.length === 0) {
            await db.query(
                `INSERT INTO doctor_personal (user_id, email, gender, dob, marital_status, about)
                VALUES ($1,$2,$3,$4,$5,$6)`,
                [userId, email, gender, dob, marital_status, about]
            )
        } else {
            await db.query(
                `UPDATE doctor_personal SET email=$1, gender=$2, dob=$3,
                marital_status=$4, about=$5 WHERE user_id=$6`,
                [email, gender, dob, marital_status, about, userId]
            )
        }
        res.json({ message: 'Updated successfully' })
    } catch (error) {
        res.status(500).json({ message: 'Server error' })
    }
}

export const updateDoctorProfessional = async (req: Request, res: Response) => {
    const { userId } = req.params
    const { license_number, speciality, sub_speciality, qualification, experience_years, languages, consultation_fee } = req.body
    try {
        const existing = await db.query(
            'SELECT * FROM doctor_professional WHERE user_id = $1', [userId]
        )
        if (existing.rows.length === 0) {
            await db.query(
                `INSERT INTO doctor_professional
                (user_id, license_number, speciality, sub_speciality, qualification, experience_years, languages, consultation_fee)
                VALUES ($1,$2,$3,$4,$5,$6,$7,$8)`,
                [userId, license_number, speciality, sub_speciality, qualification, experience_years, languages, consultation_fee]
            )
        } else {
            await db.query(
                `UPDATE doctor_professional SET license_number=$1, speciality=$2,
                sub_speciality=$3, qualification=$4, experience_years=$5,
                languages=$6, consultation_fee=$7 WHERE user_id=$8`,
                [license_number, speciality, sub_speciality, qualification, experience_years, languages, consultation_fee, userId]
            )
        }
        res.json({ message: 'Updated successfully' })
    } catch (error) {
        res.status(500).json({ message: 'Server error' })
    }
}

export const updateDoctorClinic = async (req: Request, res: Response) => {
    const { userId } = req.params
    const { clinic_name, address, city, pincode } = req.body
    try {
        const existing = await db.query(
            'SELECT * FROM doctor_clinic WHERE user_id = $1', [userId]
        )
        if (existing.rows.length === 0) {
            await db.query(
                `INSERT INTO doctor_clinic (user_id, clinic_name, address, city, pincode)
                VALUES ($1,$2,$3,$4,$5)`,
                [userId, clinic_name, address, city, pincode]
            )
        } else {
            await db.query(
                `UPDATE doctor_clinic SET clinic_name=$1, address=$2,
                city=$3, pincode=$4 WHERE user_id=$5`,
                [clinic_name, address, city, pincode, userId]
            )
        }
        res.json({ message: 'Updated successfully' })
    } catch (error) {
        res.status(500).json({ message: 'Server error' })
    }
}

export const updateDoctorAvailability = async (req: Request, res: Response) => {
    const { userId } = req.params
    const { day_of_week, start_time, end_time, slot_duration_minutes } = req.body
    try {
        const existing = await db.query(
            'SELECT * FROM doctor_availability WHERE user_id = $1 AND day_of_week = $2',
            [userId, day_of_week]
        )
        if (existing.rows.length === 0) {
            await db.query(
                `INSERT INTO doctor_availability
                (user_id, day_of_week, start_time, end_time, slot_duration_minutes)
                VALUES ($1,$2,$3,$4,$5)`,
                [userId, day_of_week, start_time, end_time, slot_duration_minutes]
            )
        } else {
            await db.query(
                `UPDATE doctor_availability SET start_time=$1, end_time=$2,
                slot_duration_minutes=$3 WHERE user_id=$4 AND day_of_week=$5`,
                [start_time, end_time, slot_duration_minutes, userId, day_of_week]
            )
        }
        res.json({ message: 'Updated successfully' })
    } catch (error) {
        res.status(500).json({ message: 'Server error' })
    }
}

export const searchDoctors = async (req: Request, res: Response) => {
    const {
        q = '',                    
        consultation_type,         
        min_experience,            
        max_fee,                   
        min_fee,                   
        languages,                 
    } = req.query

    const lastSync = (req.query.since as string) || '1970-01-01T00:00:00Z';

    try {
        const conditions: string[] = ['u.role = \'doctor\'']
        const params: any[] = []
        let paramIndex = 1

        if (q) {
            conditions.push(`(
                u.name ILIKE $${paramIndex} OR 
                dp.speciality ILIKE $${paramIndex}
            )`)
            params.push(`%${q}%`)
            paramIndex++
        }

        if (consultation_type && consultation_type !== 'both') {
            conditions.push(`(
                dp.consultation_type = $${paramIndex} OR 
                dp.consultation_type = 'both'
            )`)
            params.push(consultation_type)
            paramIndex++
        }

        if (min_experience) {
            conditions.push(`dp.experience_years >= $${paramIndex}`)
            params.push(Number(min_experience))
            paramIndex++
        }

        if (min_fee) {
            conditions.push(`dp.consultation_fee >= $${paramIndex}`)
            params.push(Number(min_fee))
            paramIndex++
        }
        if (max_fee) {
            conditions.push(`dp.consultation_fee <= $${paramIndex}`)
            params.push(Number(max_fee))
            paramIndex++
        }

        if (languages) {
            conditions.push(`dp.languages ILIKE $${paramIndex}`)
            params.push(`%${languages}%`)
            paramIndex++
        }

        conditions.push(`(
            u.updated_at > $${paramIndex} OR 
            dp.updated_at > $${paramIndex} OR 
            dper.updated_at > $${paramIndex} OR 
            dc.updated_at > $${paramIndex}
        )`)
        params.push(lastSync)
        paramIndex++

        const whereClause = conditions.join(' AND ')

        const result = await db.query(`
            SELECT 
                u.id              AS doctor_id,
                u.name            AS doctor_name,
                u.profile_photo_key AS profile_photo,
                dp.speciality,
                dp.sub_speciality,
                dp.qualification,
                dp.experience_years,
                dp.languages,
                dp.consultation_fee,
                dp.consultation_type,
                dper.about,
                dc.city,
                GREATEST(
                    u.updated_at, 
                    COALESCE(dp.updated_at, '1970-01-01'::timestamp), 
                    COALESCE(dper.updated_at, '1970-01-01'::timestamp), 
                    COALESCE(dc.updated_at, '1970-01-01'::timestamp)
                ) AS updated_at
            FROM users u
            LEFT JOIN doctor_professional dp   ON u.id = dp.user_id
            LEFT JOIN doctor_personal     dper ON u.id = dper.user_id
            LEFT JOIN doctor_clinic       dc   ON u.id = dc.user_id
            WHERE ${whereClause}
            ORDER BY dp.experience_years DESC NULLS LAST
            LIMIT 20
        `, params)

        res.json({ doctors: result.rows })

    } catch (error) {
        res.status(500).json({ message: 'Server error' })
    }
}

export const getDoctorProfile = async (req: Request, res: Response) => {
    const { doctorId } = req.params
    const lastSync = (req.query.since as string) || '1970-01-01T00:00:00Z';

    try {
        const result = await db.query(`
            SELECT 
                u.id              AS doctor_id,
                u.name            AS doctor_name,
                u.profile_photo_key AS profile_photo,
                dp.speciality,
                dp.sub_speciality,
                dp.qualification,
                dp.experience_years,
                dp.consultation_fee,
                dp.consultation_type,
                dp.languages,
                dp.license_number,
                dper.about,
                dper.gender,
                dper.email,
                dper.dob,
                dper.marital_status,
                dc.clinic_name,
                dc.address,
                dc.city,
                dc.pincode,
                dc.lat,
                dc.lng,
                GREATEST(
                    u.updated_at, 
                    COALESCE(dp.updated_at, '1970-01-01'::timestamp), 
                    COALESCE(dper.updated_at, '1970-01-01'::timestamp), 
                    COALESCE(dc.updated_at, '1970-01-01'::timestamp)
                ) AS updated_at
            FROM users u
            LEFT JOIN doctor_professional dp   ON u.id = dp.user_id
            LEFT JOIN doctor_personal     dper ON u.id = dper.user_id
            LEFT JOIN doctor_clinic       dc   ON u.id = dc.user_id
            WHERE u.id = $1 AND u.role = 'doctor'
            AND (
                u.updated_at > $2 OR 
                dp.updated_at > $2 OR 
                dper.updated_at > $2 OR 
                dc.updated_at > $2
            )
        `, [doctorId, lastSync])

        if (result.rows.length === 0) {
            if (req.query.since) {
                return res.json({ doctor: null, notModified: true })
            }
            return res.status(404).json({ message: 'Doctor not found' })
        }

        res.json({ doctor: result.rows[0] })
    } catch (error) {
        res.status(500).json({ message: 'Server error' })
    }
}