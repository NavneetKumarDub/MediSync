import { Router } from 'express'
import { bookAppointment , getPatientAppointments,getDoctorAppointments} from '../controllers/appointments.controller'
import { authMiddleware } from '../middlewares/auth.middleware'

const router = Router()
router.post('/book', authMiddleware, bookAppointment)
router.get('/patient', authMiddleware, getPatientAppointments)
router.get('/doctor', authMiddleware, getDoctorAppointments)
export default router