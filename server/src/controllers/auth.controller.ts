import { Request, Response } from 'express'
import jwt from 'jsonwebtoken'
import { firebaseAuth } from '../config/firebase'
import db from '../config/db'

export async function verifyOtp(req: Request, res: Response) {
    console.log("Inside auth controller")
    const { idToken } = req.body

    if (!idToken) {
        return res.status(400).json({ error: 'idToken required' })
    }

    try {
        // 1. Verify Firebase idToken — proves phone was verified
        const decoded = await firebaseAuth.verifyIdToken(idToken)
        const phone = decoded.phone_number?.replace(/^\+91/, '') // strip +91, keep 10 digits

        if (!phone) {
            return res.status(400).json({ error: 'No phone number in token' })
        }

        // 2. Check if user exists in our DB
        const existing = await db.query(
            `SELECT id, phone, name, role FROM users WHERE phone = $1`,
            [phone]
        )

        let user
        let isNewUser = false

        if (existing.rows.length > 0) {
            // Returning user
            user = existing.rows[0]
        } else {
            // New user — create with minimal info. Name + role filled later.
            const created = await db.query(
                `INSERT INTO users (phone) VALUES ($1) RETURNING id, phone, name, role`,
                [phone]
            )
            user = created.rows[0]
            isNewUser = true
        }

        // 3. Issue our own JWT
        const token = jwt.sign(
            { id: user.id, role: user.role },
            process.env.JWT_SECRET!,
            { expiresIn: '30d' }
        )

        res.json({ token, user, isNewUser })
    } catch (err: any) {
        console.error('verify-otp error:', err)
        res.status(401).json({ error: 'Invalid token' })
    }
}