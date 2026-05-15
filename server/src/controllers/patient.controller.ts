import { Request, Response } from 'express'
import db from '../config/db'

export const getPersonalProfile = async (req: Request, res: Response) => {
    const { userId } = req.params
    const lastSync = (req.query.since as string) || '1970-01-01T00:00:00Z';

    try {
        const result = await db.query(
            `SELECT 
                p.email,
                p.gender,
                p.dob,
                p.blood_group,
                p.marital_status,
                p.height,
                p.weight,
                p.emergency_contact,
                u.name, 
                u.profile_photo_key AS profile_photo,
                GREATEST(p.updated_at, u.updated_at) AS updated_at
             FROM patient_personal as p 
             JOIN users as u on p.user_id = u.id 
             WHERE p.user_id = $1 
             AND (p.updated_at > $2 OR u.updated_at > $2)`, 
            [userId, lastSync]
        )
        res.json({ data: result.rows[0] || null })
    } catch (error) {
        res.status(500).json({ message: 'Server error' })
    }
}

export const updatePersonalProfile = async (req: Request, res: Response) => {
    const { userId } = req.params
    const { name, email, gender, dob, blood_group, marital_status, height, weight, emergency_contact } = req.body
    try {
        const existing = await db.query(
            'SELECT * FROM patient_personal WHERE user_id = $1', [userId]
        )
        if (existing.rows.length === 0) {
            try {
                await db.query(
                    `INSERT INTO patient_personal 
                    (user_id, email, gender, dob, blood_group, marital_status, height, weight, emergency_contact)
                    VALUES ($1,$2,$3,$4,$5,$6,$7,$8,$9)`,
                    [userId, email, gender, dob, blood_group, marital_status, height, weight, emergency_contact]
                )
            } catch (insertError) {
                console.error("INSERT ERROR:", insertError)  
            }
        } else {
            try {
                await db.query('BEGIN');
                await db.query(
                    `UPDATE patient_personal SET
                    email = $1, gender = $2, dob = $3, blood_group = $4,
                    marital_status = $5, height = $6, weight = $7, emergency_contact = $8
                    WHERE user_id = $9`,
                    [email, gender, dob, blood_group, marital_status, height, weight, emergency_contact, userId]
                );
                await db.query(
                    `UPDATE users SET name = $1 WHERE id = $2`,
                    [name, userId]
                );
                await db.query('COMMIT');
            } catch (err) {
                await db.query('ROLLBACK');
                throw err;
            }
        }
        res.json({ message: 'Profile updated successfully' })
    } catch (error) {
        res.status(500).json({ message: 'Server error' })
    }
}

export const getMedicalProfile = async (req: Request, res: Response) => {
    const { userId } = req.params
    const lastSync = (req.query.since as string) || '1970-01-01T00:00:00Z';

    try {
        const result = await db.query(
            `SELECT * FROM patient_medical 
             WHERE user_id = $1 AND updated_at > $2`, 
            [userId, lastSync]
        )
        res.json({ data: result.rows[0] || null })
    } catch (error) {
        res.status(500).json({ message: 'Server error' })
    }
}

export const updateMedicalProfile = async (req: Request, res: Response) => {
    const { userId } = req.params
    const { allergies, current_medications, past_medications, chronic_diseases, injuries, surgeries } = req.body
    try {
        const existing = await db.query(
            'SELECT * FROM patient_medical WHERE user_id = $1', [userId]
        )
        if (existing.rows.length === 0) {
            await db.query(
                `INSERT INTO patient_medical
                (user_id, allergies, current_medications, past_medications, chronic_diseases, injuries, surgeries)
                VALUES ($1,$2,$3,$4,$5,$6,$7)`,
                [userId, allergies, current_medications, past_medications, chronic_diseases, injuries, surgeries]
            )
        } else {
            await db.query(
                `UPDATE patient_medical SET
                allergies=$1, current_medications=$2, past_medications=$3,
                chronic_diseases=$4, injuries=$5, surgeries=$6
                WHERE user_id=$7`,
                [allergies, current_medications, past_medications, chronic_diseases, injuries, surgeries, userId]
            )
        }
        res.json({ message: 'Medical profile updated successfully' })
    } catch (error) {
        res.status(500).json({ message: 'Server error' })
    }
}

export const getLifestyleProfile = async (req: Request, res: Response) => {
    const { userId } = req.params
    const lastSync = (req.query.since as string) || '1970-01-01T00:00:00Z';

    try {
        const result = await db.query(
            `SELECT * FROM patient_lifestyle 
             WHERE user_id = $1 AND updated_at > $2`, 
            [userId, lastSync]
        )
        res.json({ data: result.rows[0] || null })
    } catch (error) {
        res.status(500).json({ message: 'Server error' })
    }
}

export const updateLifestyleProfile = async (req: Request, res: Response) => {
    const { userId } = req.params
    const { smoking, alcohol, activity_level, food_preference, occupation } = req.body
    try {
        const existing = await db.query(
            'SELECT * FROM patient_lifestyle WHERE user_id = $1', [userId]
        )
        if (existing.rows.length === 0) {
            await db.query(
                `INSERT INTO patient_lifestyle
                (user_id, smoking, alcohol, activity_level, food_preference, occupation)
                VALUES ($1,$2,$3,$4,$5,$6)`,
                [userId, smoking, alcohol, activity_level, food_preference, occupation]
            )
        } else {
            await db.query(
                `UPDATE patient_lifestyle SET
                smoking=$1, alcohol=$2, activity_level=$3, food_preference=$4, occupation=$5
                WHERE user_id=$6`,
                [smoking, alcohol, activity_level, food_preference, occupation, userId]
            )
        }
        res.json({ message: 'Lifestyle profile updated successfully' })
    } catch (error) {
        res.status(500).json({ message: 'Server error' }) 
    }
}