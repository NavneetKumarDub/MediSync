import cron from 'node-cron'
import db from '../config/db'

const getDayName = (date: Date): string => {
    const days = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat']
    return days[date.getDay()]
}

const buildLocalDateStr = (date: Date): string => {
    const y = date.getFullYear()
    const m = String(date.getMonth() + 1).padStart(2, '0')
    const d = String(date.getDate()).padStart(2, '0')
    return `${y}-${m}-${d}`
}

const parseUTCDateFromPg = (rawDate: any): Date => {
    if (rawDate instanceof Date) {
        // pg returns DATE as midnight UTC — extract UTC parts to avoid IST shift
        const y = rawDate.getUTCFullYear()
        const mo = rawDate.getUTCMonth()
        const d = rawDate.getUTCDate()
        return new Date(y, mo, d) // local midnight, safe for getDay()
    }
    // fallback: string like "2026-05-12"
    const [y, m, d] = String(rawDate).split('T')[0].split('-').map(Number)
    return new Date(y, m - 1, d)
}

const generateSlotsForNextDay = async () => {
    try {
        // Step 1: Get all doctors who have availability templates
        const allDoctors = await db.query(
            `SELECT DISTINCT user_id FROM doctor_availability WHERE is_active = TRUE`
        )

        if (allDoctors.rows.length === 0) {
            console.log('[SlotGenerator] No active doctors found')
            return
        }

        console.log(`[SlotGenerator] Processing ${allDoctors.rows.length} doctors`)

        for (const doctor of allDoctors.rows) {
            const doctorId = doctor.user_id

            try {
                // Step 2: Get last slot date for this doctor
                const lastDateResult = await db.query(
                    `SELECT MAX(date) as last_date FROM appointment_slots WHERE doctor_id = $1`,
                    [doctorId]
                )

                let targetDate: Date

                if (!lastDateResult.rows[0].last_date) {
                    // No slots yet — start from tomorrow
                    const tomorrow = new Date()
                    tomorrow.setHours(0, 0, 0, 0)
                    tomorrow.setDate(tomorrow.getDate() + 1)
                    targetDate = tomorrow
                } else {
                    const parsed = parseUTCDateFromPg(lastDateResult.rows[0].last_date)
                    parsed.setDate(parsed.getDate() + 1)
                    targetDate = parsed
                }

                const targetDateStr = buildLocalDateStr(targetDate)
                const targetDay     = getDayName(targetDate)

                console.log(`[SlotGenerator] Doctor ${doctorId} — generating for ${targetDateStr} (${targetDay})`)

                // Step 3: Get templates for this doctor for that day
                const templates = await db.query(
                    `SELECT
                        user_id,
                        start_time,
                        end_time,
                        slot_duration_minutes,
                        consultation_fee,
                        consultation_type
                     FROM doctor_availability
                     WHERE user_id = $1
                       AND day_of_week = $2
                       AND is_active = TRUE`,
                    [doctorId, targetDay]
                )

                if (templates.rows.length === 0) {
                    console.log(`[SlotGenerator] Doctor ${doctorId} — no template for ${targetDay}, skipping`)
                    continue
                }

                // Step 4: Insert slots
                let inserted = 0
                let skipped  = 0

                for (const template of templates.rows) {
                    const slotDuration = template.slot_duration_minutes

                    const [startHour, startMin] = template.start_time.split(':').map(Number)
                    const [endHour, endMin]     = template.end_time.split(':').map(Number)

                    let currentMinutes   = startHour * 60 + startMin
                    const endMinutes     = endHour * 60 + endMin

                    while (currentMinutes + slotDuration <= endMinutes) {
                        const slotStartHour = Math.floor(currentMinutes / 60)
                        const slotStartMin  = currentMinutes % 60
                        const slotEndHour   = Math.floor((currentMinutes + slotDuration) / 60)
                        const slotEndMin    = (currentMinutes + slotDuration) % 60

                        const slotStart = `${String(slotStartHour).padStart(2, '0')}:${String(slotStartMin).padStart(2, '0')}`
                        const slotEnd   = `${String(slotEndHour).padStart(2, '0')}:${String(slotEndMin).padStart(2, '0')}`

                        const result = await db.query(
                            `INSERT INTO appointment_slots
                                (doctor_id, date, start_time, end_time, consultation_fee, consultation_type, slot_duration_minutes, status, is_active)
                             VALUES ($1, $2, $3, $4, $5, $6, $7, 'available', TRUE)
                             ON CONFLICT (doctor_id, date, start_time) DO NOTHING
                             RETURNING id`,
                            [
                                doctorId,
                                targetDateStr,
                                slotStart,
                                slotEnd,
                                template.consultation_fee,
                                template.consultation_type,
                                slotDuration
                            ]
                        )

                        if (result.rows.length > 0) {
                            inserted++
                        } else {
                            skipped++
                        }

                        currentMinutes += slotDuration
                    }
                }

                console.log(`[SlotGenerator] Doctor ${doctorId} — inserted: ${inserted}, skipped: ${skipped}`)

            } catch (err) {
                console.error(`[SlotGenerator] Error processing doctor ${doctorId}:`, err)
            }
        }

        console.log('[SlotGenerator] All doctors processed')

    } catch (error) {
        console.error('[SlotGenerator] Fatal error:', error)
    }
}

export const startSlotGeneratorJob = () => {
    cron.schedule('17 20 * * *', async () => {
        console.log('[SlotGenerator] Cron triggered at 11 PM')
        await generateSlotsForNextDay()
    }, {
        timezone: 'Asia/Kolkata'
    })

    console.log('[SlotGenerator] Cron job scheduled — runs at 11 PM IST daily')
}

export { generateSlotsForNextDay }