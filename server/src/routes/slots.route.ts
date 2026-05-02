import { Router } from 'express'
import {
    getDoctorAvailableDates,
    getDoctorSlots,
    getDoctorAvailability
} from '../controllers/slots.controller'

const router = Router()

// Get available dates for a doctor
router.get('/:doctorId/dates', getDoctorAvailableDates)

// Get slots for a specific date
router.get('/:doctorId/slots', getDoctorSlots)
router.get('/:doctorId/availability', getDoctorAvailability)  // ← add


export default router