"use strict";
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.verifyOtp = verifyOtp;
const jsonwebtoken_1 = __importDefault(require("jsonwebtoken"));
const firebase_1 = require("../config/firebase");
const db_1 = __importDefault(require("../config/db"));
async function verifyOtp(req, res) {
    console.log("Inside auth controller");
    const { idToken } = req.body;
    if (!idToken) {
        return res.status(400).json({ error: 'idToken required' });
    }
    try {
        const decoded = await firebase_1.firebaseAuth.verifyIdToken(idToken);
        const phone = decoded.phone_number?.replace(/^\+91/, '');
        if (!phone) {
            return res.status(400).json({ error: 'No phone number in token' });
        }
        const existing = await db_1.default.query(`SELECT id, phone, name, role FROM users WHERE phone = $1`, [phone]);
        let user;
        let isNewUser = false;
        if (existing.rows.length > 0) {
            user = existing.rows[0];
        }
        else {
            const created = await db_1.default.query(`INSERT INTO users (phone) VALUES ($1) RETURNING id, phone, name, role`, [phone]);
            user = created.rows[0];
            isNewUser = true;
        }
        const token = jsonwebtoken_1.default.sign({ id: user.id, role: user.role }, process.env.JWT_SECRET, { expiresIn: '30d' });
        res.json({ token, user, isNewUser });
    }
    catch (err) {
        console.error('verify-otp error:', err);
        res.status(401).json({ error: 'Invalid token' });
    }
}
