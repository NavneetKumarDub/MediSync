import db from '../config/db';
import { Request,Response } from 'express';

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
                user: updated.rows[0]
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
            user: result.rows[0]
        });
    } catch (error) {
        console.error("registerUser error:", error);
        res.status(500).json({ message: "Server error" });
    }
};