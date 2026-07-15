import db from '../config/db';
import { Request,Response } from 'express';
import jwt from 'jsonwebtoken';

const createUserToken = (user: { id: number; role: string }) => jwt.sign(
    { id: user.id, role: user.role },
    process.env.JWT_SECRET!,
    { expiresIn: '30d' }
);

export const registerUser = async (req: Request, res: Response) => {
    const { phone, name, role } = req.body;
    console.log("register user body:", req.body);

    if (!phone || !name || !role) {
        res.status(400).json({ message: "All fields are required" });
        return;
    }

    try {
        const existing = await db.query(
            `SELECT * FROM users WHERE phone = $1`,
            [phone]
        );

        if (existing.rows.length > 0) {
            
            const updated = await db.query(
                `UPDATE users
                   SET name = $1, role = $2
                 WHERE phone = $3
             RETURNING *`,
                [name, role, phone]
            );
            res.json({
                message: "User profile completed",
                user: updated.rows[0],
                token: createUserToken(updated.rows[0])
            });
            return;
        }

        const result = await db.query(
            `INSERT INTO users (phone, name, role)
             VALUES ($1, $2, $3)
             RETURNING *`,
            [phone, name, role]
        );

        res.json({
            message: "User registered successfully",
            user: result.rows[0],
            token: createUserToken(result.rows[0])
        });
    } catch (error) {
        console.error("registerUser error:", error);
        res.status(500).json({ message: "Server error" });
    }
};

export const saveFcmToken = async (req: Request, res: Response) => {
    const userId = (req as any).user.id
    const { token, platform = 'android' } = req.body

    if (!token) {
        return res.status(400).json({ message: 'FCM token is required' })
    }

    try {
        await db.query(
            `INSERT INTO user_fcm_tokens (user_id, token, platform, updated_at)
             VALUES ($1, $2, $3, NOW())
             ON CONFLICT (token)
             DO UPDATE SET 
                user_id = EXCLUDED.user_id,
                platform = EXCLUDED.platform,
                updated_at = NOW()`,
            [userId, token, platform]
        )

        res.json({ message: 'FCM token saved' })
    } catch (error) {
        console.error('saveFcmToken error:', error)
        res.status(500).json({ message: 'Server error' })
    }
}
