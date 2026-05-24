import { Router } from 'express'
import { getPatientRecords } from '../controllers/records.controller'
import { authMiddleware } from '../middlewares/auth.middleware'

const router = Router()

router.use(authMiddleware)

router.get('/patient', getPatientRecords)

export default router