import express from 'express'
import dotenv from 'dotenv'
import { createServer } from 'http'
import db from './src/config/db'
import appointmentRoutes from './src/routes/appointment.route'
import { initChatWebSocket } from './src/websocket/chat.ws'
import { initVideoWebSocket } from './src/websocket/video.ws'
import patientRoutes from './src/routes/patient.route'
import doctorRoutes from './src/routes/doctor.route'
import slotsRoutes from './src/routes/slots.route'
import userRoutes from './src/routes/user.route'
import authRoutes from './src/routes/auth.route'
import chatRoutes from './src/routes/chat.route'
import { initMinIO } from './src/config/minio'
import uploadRoutes from './src/routes/upload.route'
import recordsRoutes from './src/routes/records.route'
import ratingRoutes from './src/routes/rating.route'

import { startSlotGeneratorJob, generateSlotsForNextDay } from './src/jobs/slot.generator'

dotenv.config()

const app = express()
const server = createServer(app)

app.use(express.json())
app.use('/api/auth',authRoutes)
app.use('/api/user', userRoutes)
app.use('/api/slots', slotsRoutes)
app.use('/api/doctor', doctorRoutes);
app.use('/api/patient',patientRoutes);
app.use('/api/appointments', appointmentRoutes)
app.use('/api/chat', chatRoutes)
app.use('/api/upload', uploadRoutes)
app.use('/api/records', recordsRoutes)
app.use('/api/ratings', ratingRoutes)



app.get('/', (req, res) => {
    res.json({ message: 'MediSync API running' })
})

db.query('SELECT NOW()')
    .then(() => {
        initMinIO()
        console.log('Database connected ')
        startSlotGeneratorJob()
    })
    .catch((err) => console.log('Database error:', err))


initChatWebSocket(server)
initVideoWebSocket(server)

const PORT = Number(process.env.PORT) || 3000 
server.listen(PORT,'0.0.0.0', () => {
    console.log(`Server running on port ${PORT}`)
})