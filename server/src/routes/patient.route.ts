import { Router } from 'express'
import {
    getPersonalProfile, updatePersonalProfile,
    getMedicalProfile, updateMedicalProfile,
    getLifestyleProfile, updateLifestyleProfile
} from '../controllers/patient.controller'
import { authMiddleware } from '../middlewares/auth.middleware'

const router = Router()

router.use(authMiddleware)

router.get('/personal/:userId', getPersonalProfile)
router.put('/personal/:userId', updatePersonalProfile)

router.get('/medical/:userId', getMedicalProfile)
router.put('/medical/:userId', updateMedicalProfile)

router.get('/lifestyle/:userId', getLifestyleProfile)
router.put('/lifestyle/:userId', updateLifestyleProfile)

export default router