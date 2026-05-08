import { Request, Response } from 'express'
import minioClient, { BUCKETS } from '../config/minio'
import db from '../config/db'

export const getPresignedUploadUrl = async (req: Request, res: Response) => {
    try {
        const { userId, fileName, fileType } = req.body

        if (!userId || !fileName || !fileType) {
            return res.status(400).json({ message: 'userId, fileName and fileType are required' })
        }

        const timestamp = Date.now()
        const extension = fileName.split('.').pop()
        const key = `users/${userId}/avatar_${timestamp}.${extension}`

        const uploadUrl = await minioClient.presignedPutObject(
            BUCKETS.PROFILE_PHOTOS,
            key,
            15 * 60
        )

        return res.status(200).json({
            uploadUrl,
            key
        })

    } catch (error) {
        console.error('Error generating presigned upload URL:', error)
        return res.status(500).json({ message: 'Failed to generate upload URL' })
    }
}

export const confirmProfilePhotoUpload = async (req: Request, res: Response) => {
    try {
        const { userId, key } = req.body

        if (!userId || !key) {
            return res.status(400).json({ message: 'userId and key are required' })
        }

        await db.query(
            'UPDATE users SET profile_photo_key = $1 WHERE id = $2',
            [key, userId]
        )

        return res.status(200).json({ message: 'Profile photo updated successfully', key })

    } catch (error) {
        console.error('Error confirming upload:', error)
        return res.status(500).json({ message: 'Failed to confirm upload' })
    }
}

export const getProfilePhotoUrl = async (req: Request, res: Response) => {
    try {
        const { userId } = req.params

        const result = await db.query(
            'SELECT profile_photo_key FROM users WHERE id = $1',
            [userId]
        )

        if (!result.rows[0] || !result.rows[0].profile_photo_key) {
            return res.status(404).json({ message: 'No profile photo found' })
        }

        const key = result.rows[0].profile_photo_key

        const viewUrl = await minioClient.presignedGetObject(
            BUCKETS.PROFILE_PHOTOS,
            key,
            60 * 60
        )

        return res.status(200).json({ viewUrl })

    } catch (error) {
        console.error('Error generating view URL:', error)
        return res.status(500).json({ message: 'Failed to generate view URL' })
    }
}