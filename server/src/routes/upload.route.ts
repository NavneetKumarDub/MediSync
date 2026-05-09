import { Router } from 'express'
import {
    getPresignedUploadUrl,
    confirmProfilePhotoUpload,
    getProfilePhotoUrl,
    deleteProfilePhoto
} from '../controllers/upload.controller'
import { authMiddleware } from '../middlewares/auth.middleware'

const router = Router()

router.use(authMiddleware)

router.post('/presigned-url', getPresignedUploadUrl)
router.post('/confirm', confirmProfilePhotoUpload)
router.get('/profile/:userId', getProfilePhotoUrl)
router.delete('/profile/:userId', deleteProfilePhoto)

export default router