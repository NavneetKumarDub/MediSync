import { Router } from 'express'
import {
    getUploadUrl,
    confirmProfilePhotoUpload,
    getProfilePhotoUrl,
    deleteProfilePhoto,
    getChatFileUploadUrl,
    getChatFileViewUrl
} from '../controllers/upload.controller'
import { authMiddleware } from '../middlewares/auth.middleware'

const router = Router()

router.use(authMiddleware)

router.post('/presigned-url', getUploadUrl)
router.post('/confirm', confirmProfilePhotoUpload)
router.get('/profile/:userId', getProfilePhotoUrl)
router.delete('/profile/:userId', deleteProfilePhoto)
router.post('/chat-file/presigned-url', getChatFileUploadUrl)
router.get('/chat-file/view-url', getChatFileViewUrl)
export default router