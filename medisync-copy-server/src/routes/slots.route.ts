import { Router } from 'express'
import {
    getDoctorAvailableDates,
    getDoctorSlots,
    addRegularSlot,
    deleteRegularSlot,
    getRegularSlots,
    createCustomSlot,
    deleteCustomSlot,
    getSlotsByDate,
    getDatesWithSlots
} from '../controllers/slots.controller'
import { authMiddleware } from '../middlewares/auth.middleware';

const router = Router()

router.use(authMiddleware);


router.post('/regular', addRegularSlot)
router.delete('/regular/:slotId', deleteRegularSlot)
router.get('/regular', getRegularSlots)

router.get('/custom/dates', getDatesWithSlots)
router.get('/custom', getSlotsByDate)
router.post('/custom', createCustomSlot)
router.delete('/custom/:slotId', deleteCustomSlot)

router.get('/:doctorId/dates', getDoctorAvailableDates)
router.get('/:doctorId/slots', getDoctorSlots)

export default router