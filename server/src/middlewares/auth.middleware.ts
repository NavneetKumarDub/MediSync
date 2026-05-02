import { Request, Response, NextFunction } from 'express'
import jwt from 'jsonwebtoken'

interface JwtPayload {
    id: number
    role: 'doctor' | 'patient'
}

// Extend Express Request to include `user`
declare global {
    namespace Express {
        interface Request {
            user?: JwtPayload
        }
    }
}

export function authMiddleware(req: Request, res: Response, next: NextFunction) {
    const header = req.headers.authorization

    if (!header || !header.startsWith('Bearer ')) {
        return res.status(401).json({ error: 'Missing or invalid token' })
    }

    const token = header.substring(7) // strip "Bearer "

    try {
        const payload = jwt.verify(token, process.env.JWT_SECRET!) as JwtPayload
        req.user = payload
        next()
    } catch {
        return res.status(401).json({ error: 'Invalid or expired token' })
    }
}