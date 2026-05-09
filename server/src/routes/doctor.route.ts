import { Router } from 'express'
import {
    updateDoctorPersonal,
    updateDoctorProfessional,
    updateDoctorClinic,
    updateDoctorAvailability,
    searchDoctors,
    getDoctorProfile
} from '../controllers/doctor.controller'
import { authMiddleware } from '../middlewares/auth.middleware'

const router = Router()

router.use(authMiddleware)

router.put('/personal/:userId', updateDoctorPersonal)
router.put('/professional/:userId', updateDoctorProfessional)
router.put('/clinic/:userId', updateDoctorClinic)
router.put('/availability/:userId', updateDoctorAvailability)


router.get('/search',                searchDoctors)
router.get('/profile/:doctorId',     getDoctorProfile)

export default router