import { Request, Response } from 'express'
import db from '../config/db'

// GET personal profile
export const getPersonalProfile = async (req: Request, res: Response) => {
    const { userId } = req.params
    try {
        const result = await db.query(
            'SELECT p.*,u.name FROM patient_personal as p join users as u on p.user_id = u.id where p.user_id = $1', [userId]
        )
        res.json({ data: result.rows[0] || null })
    } catch (error) {
        res.status(500).json({ message: 'Server error' })
    }
}

// UPDATE personal profile
export const updatePersonalProfile = async (req: Request, res: Response) => {
    const { userId } = req.params
    console.log("updateProfile - ",req.body)

    const { name,email, gender, dob, blood_group, marital_status, height, weight, emergency_contact } = req.body
    try {
        const existing = await db.query(
            'SELECT * FROM patient_personal WHERE user_id = $1', [userId]
        )
        if (existing.rows.length === 0) {
            console.log("not existing - inserting with userId:", userId)
            try {
                const result = await db.query(
                    `INSERT INTO patient_personal 
                    (user_id, email, gender, dob, blood_group, marital_status, height, weight, emergency_contact)
                    VALUES ($1,$2,$3,$4,$5,$6,$7,$8,$9)`,
                    [userId, email, gender, dob, blood_group, marital_status, height, weight, emergency_contact]
                )
                console.log("INSERT result:", result.rowCount)
            } catch (insertError) {
                console.log("INSERT ERROR:", insertError)  // ← this will show exact DB error
            }
        }else {
            console.log("existing")
            try {
            await db.query('BEGIN');
 
            // patient_personal update
            await db.query(
                `UPDATE patient_personal SET
                email = $1,
                gender = $2,
                dob = $3,
                blood_group = $4,
                marital_status = $5,
                height = $6,
                weight = $7,
                emergency_contact = $8
                WHERE user_id = $9`,
                [
                email,
                gender,
                dob,
                blood_group,
                marital_status,
                height,
                weight,
                emergency_contact,
                userId
                ]
            );

            // users table update (name)
            await db.query(
                `UPDATE users
                SET name = $1
                WHERE id = $2`,
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

// GET medical profile
export const getMedicalProfile = async (req: Request, res: Response) => {
    const { userId } = req.params
    try {
        const result = await db.query(
            'SELECT * FROM patient_medical WHERE user_id = $1', [userId]
        )
        res.json({ data: result.rows[0] || null })
    } catch (error) {
        res.status(500).json({ message: 'Server error' })
    }
}

// UPDATE medical profile
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
    try {
        const result = await db.query(
            'SELECT * FROM patient_lifestyle WHERE user_id = $1', [userId]
        )
        res.json({ data: result.rows[0] || null })
    } catch (error) {
        res.status(500).json({ message: 'Server error' })
    }
}

export const updateLifestyleProfile = async (req: Request, res: Response) => {
    const { userId } = req.params
    const { smoking, alcohol, activity_level, food_preference,occupation } = req.body
    try {
        const existing = await db.query(
            'SELECT * FROM patient_lifestyle WHERE user_id = $1', [userId]
        )
        if (existing.rows.length === 0) {
            await db.query(
                `INSERT INTO patient_lifestyle
                (user_id, smoking, alcohol, activity_level, food_preference,occupation)
                VALUES ($1,$2,$3,$4,$5,$6)`,
                [userId, smoking, alcohol, activity_level, food_preference,occupation]
            )
        } else {
            await db.query(
                `UPDATE patient_lifestyle SET
                smoking=$1, alcohol=$2, activity_level=$3, food_preference=$4,occupation=$5
                WHERE user_id=$6`,
                [smoking, alcohol, activity_level, food_preference,occupation, userId]
            )
        }
        res.json({ message: 'Lifestyle profile updated successfully' })
    } catch (error) {
        res.status(500).json({ message: 'Server error' }) 
    }
}