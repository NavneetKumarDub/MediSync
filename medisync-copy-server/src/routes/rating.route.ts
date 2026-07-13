import { Router } from 'express'
import {
    submitDoctorRating,
    getAppointmentRating,
    getDoctorRatingSummary
} from '../controllers/rating.controller'
import { authMiddleware } from '../middlewares/auth.middleware'

const router = Router()

router.use(authMiddleware)

router.post('/', submitDoctorRating)
router.get('/appointment/:appointmentId', getAppointmentRating)
router.get('/doctor/:doctorId/summary', getDoctorRatingSummary)

export default router