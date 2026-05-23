import db from '../config/db'
import { firebaseMessaging } from '../config/firebase'

type NotificationPayload = {
    userId: number
    title: string
    body: string
    data?: Record<string, string>
    dataOnly?: boolean
}

export async function sendPushNotificationToUser({
    userId,
    title,
    body,
    data = {},
    dataOnly = false
}: NotificationPayload) {
    const tokenRes = await db.query(
        `SELECT token FROM user_fcm_tokens WHERE user_id = $1`,
        [userId]
    )

    const tokens = tokenRes.rows.map(row => row.token)

    if (tokens.length === 0) {
        console.log(`[FCM] No tokens found for user ${userId}`)
        return
    }

    const messagePayload: any = {
        tokens,
        data: {
            ...data,
            title,
            body
        },
        android: {
            priority: 'high',
            notification: {
                channelId: 'medisync_notifications',
                priority: 'max',
                defaultSound: true,
                defaultVibrateTimings: true,
                visibility: 'public'
            }
        }
    }

    if (!dataOnly) {
        messagePayload.notification = {
            title,
            body
        }
    }

    const response = await firebaseMessaging.sendEachForMulticast(messagePayload)

    const failedTokens: string[] = []

    response.responses.forEach((result, index) => {
        if (!result.success) {
            const errorCode = result.error?.code

            if (
                errorCode === 'messaging/registration-token-not-registered' ||
                errorCode === 'messaging/invalid-registration-token'
            ) {
                failedTokens.push(tokens[index])
            }

            console.error('[FCM] Send failed:', result.error)
        }
    })

    if (failedTokens.length > 0) {
        await db.query(
            `DELETE FROM user_fcm_tokens WHERE token = ANY($1::text[])`,
            [failedTokens]
        )
    }
}