import { Router } from 'express'
import {
    updateDoctorPersonal,
    updateDoctorProfessional,
    updateDoctorClinic,
    updateDoctorAvailability,
    searchDoctors,
    getDoctorProfile,
    getMyClinicLocation,
    updateClinicLocation
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

router.get('/clinic-location', getMyClinicLocation)
router.put('/clinic-location', updateClinicLocation)

export default router