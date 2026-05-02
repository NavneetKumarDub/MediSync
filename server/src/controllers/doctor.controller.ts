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
    console.log("Iside serach doctor controller : ",req.query);
    const {
        q = '',                    // search query (name or speciality)
        consultation_type,         // online | offline | both
        min_experience,            // minimum years
        max_fee,                   // maximum fee
        min_fee,                   // minimum fee
        languages,                 // language filter
    } = req.query

    try {
        const conditions: string[] = ['u.role = \'doctor\'']
        const params: any[] = []
        let paramIndex = 1

        // Search by name or speciality
        if (q) {
            conditions.push(`(
                u.name ILIKE $${paramIndex} OR 
                dp.speciality ILIKE $${paramIndex}
            )`)
            params.push(`%${q}%`)
            paramIndex++
        }

        // Filter by consultation type
        if (consultation_type && consultation_type !== 'both') {
            conditions.push(`(
                dp.consultation_type = $${paramIndex} OR 
                dp.consultation_type = 'both'
            )`)
            params.push(consultation_type)
            paramIndex++
        }

        // Filter by minimum experience
        if (min_experience) {
            conditions.push(`dp.experience_years >= $${paramIndex}`)
            params.push(Number(min_experience))
            paramIndex++
        }

        // Filter by fee range
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

        // Filter by language
        if (languages) {
            conditions.push(`dp.languages ILIKE $${paramIndex}`)
            params.push(`%${languages}%`)
            paramIndex++
        }

        const whereClause = conditions.join(' AND ')

        const result = await db.query(`
            SELECT 
                u.id              AS doctor_id,
                u.name            AS doctor_name,
                dp.speciality,
                dp.sub_speciality,
                dp.qualification,
                dp.experience_years,
                dp.languages,
                dp.consultation_fee,
                dp.consultation_type,
                dper.about,
                dper.profile_photo,
                dc.city
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
        console.error('Search error:', error)
        res.status(500).json({ message: 'Server error' })
    }
}

// Get single doctor public profile
export const getDoctorProfile = async (req: Request, res: Response) => {
    console.log("Inside getdoctorprofile")
    const { doctorId } = req.params
    try {
        const result = await db.query(`
            SELECT 
                u.id              AS doctor_id,
                u.name            AS doctor_name,
                dp.speciality,
                dp.sub_speciality,
                dp.qualification,
                dp.experience_years,
                dp.languages,
                dp.consultation_fee,
                dp.consultation_type,
                dper.about,
                dper.gender,
                dper.profile_photo,
                dc.clinic_name,
                dc.address,
                dc.city,
                dc.pincode
            FROM users u
            LEFT JOIN doctor_professional dp   ON u.id = dp.user_id
            LEFT JOIN doctor_personal     dper ON u.id = dper.user_id
            LEFT JOIN doctor_clinic       dc   ON u.id = dc.user_id
            WHERE u.id = $1 AND u.role = 'doctor'
        `, [doctorId])
        console.log("doctorProfile : ",result)
        if (result.rows.length === 0) {
            return res.status(404).json({ message: 'Doctor not found' })
        }

        res.json({ doctor: result.rows[0] })
    } catch (error) {
        console.log("error : ",error)
        res.status(500).json({ message: 'Server error' })
    }
}