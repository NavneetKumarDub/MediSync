import cron from 'node-cron'
import db from '../config/db'

const getDayName = (date: Date): string => {
    const days = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat']
    return days[date.getDay()]
}

const generateSlotsForTomorrow = async () => {
    try {
        const tomorrow = new Date()
        tomorrow.setDate(tomorrow.getDate() + 1)

        const tomorrowDate = tomorrow.toISOString().split('T')[0]
        const tomorrowDay  = getDayName(tomorrow)

        console.log(`[SlotGenerator] Running for ${tomorrowDate} (${tomorrowDay})`)

        const templates = await db.query(
            `SELECT 
                user_id,
                start_time,
                end_time,
                slot_duration_minutes,
                consultation_fee,
                consultation_type
             FROM doctor_availability
             WHERE day_of_week = $1
               AND is_active = TRUE`,
            [tomorrowDay]
        )

        if (templates.rows.length === 0) {
            console.log(`[SlotGenerator] No templates found for ${tomorrowDay}`)
            return
        }

        console.log(`[SlotGenerator] Found ${templates.rows.length} templates for ${tomorrowDay}`)

        let inserted = 0
        let skipped  = 0

       for (const template of templates.rows) {
            try {
                const slotDuration = template.slot_duration_minutes

                const [startHour, startMin] = template.start_time.split(':').map(Number)
                const [endHour, endMin]     = template.end_time.split(':').map(Number)

                let currentMinutes = startHour * 60 + startMin
                const endMinutes   = endHour * 60 + endMin

                while (currentMinutes + slotDuration <= endMinutes) {
                    const slotStartHour = Math.floor(currentMinutes / 60)
                    const slotStartMin  = currentMinutes % 60
                    const slotEndHour   = Math.floor((currentMinutes + slotDuration) / 60)
                    const slotEndMin    = (currentMinutes + slotDuration) % 60

                    const slotStart = `${String(slotStartHour).padStart(2, '0')}:${String(slotStartMin).padStart(2, '0')}`
                    const slotEnd   = `${String(slotEndHour).padStart(2, '0')}:${String(slotEndMin).padStart(2, '0')}`

                    await db.query(
                        `INSERT INTO appointment_slots
                            (doctor_id, date, start_time, end_time, consultation_fee, consultation_type, slot_duration_minutes, status, is_active)
                        VALUES ($1, $2, $3, $4, $5, $6, $7, 'available', TRUE)
                        ON CONFLICT (doctor_id, date, start_time) DO NOTHING`,
                        [
                            template.user_id,
                            tomorrowDate,
                            slotStart,
                            slotEnd,
                            template.consultation_fee,
                            template.consultation_type,
                            slotDuration
                        ]
                    )

                    inserted++
                    currentMinutes += slotDuration
                }
            } catch (err) {
                skipped++
                console.error(`[SlotGenerator] Failed to insert slot for doctor ${template.user_id}:`, err)
            }
}

        console.log(`[SlotGenerator] Done — inserted: ${inserted}, skipped: ${skipped}`)

    } catch (error) {
        console.error('[SlotGenerator] Error generating slots:', error)
    }
}

export const startSlotGeneratorJob = () => {
    cron.schedule('0 23 * * *', async () => {
        console.log('[SlotGenerator] Cron triggered at 11 PM')
        await generateSlotsForTomorrow()
    }, {
        timezone: 'Asia/Kolkata'
    })

    console.log('[SlotGenerator] Cron job scheduled — runs at 11 PM IST daily')
}

export { generateSlotsForTomorrow }