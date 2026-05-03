import { Router } from 'express'
import {
    getDoctorAvailableDates,
    getDoctorSlots,
    addRegularSlot,
    deleteRegularSlot,
    getRegularSlots,
} from '../controllers/slots.controller'
import { authMiddleware } from '../middlewares/auth.middleware';

const router = Router()

router.use(authMiddleware);

router.get('/:doctorId/dates', getDoctorAvailableDates)
router.get('/:doctorId/slots', getDoctorSlots)

router.post('/regular', addRegularSlot)
router.delete('/regular/:slotId', deleteRegularSlot)
router.get('/regular', getRegularSlots)

export default router