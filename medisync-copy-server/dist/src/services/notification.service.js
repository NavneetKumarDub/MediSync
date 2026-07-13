"use strict";
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.sendPushNotificationToUser = sendPushNotificationToUser;
const db_1 = __importDefault(require("../config/db"));
const firebase_1 = require("../config/firebase");
async function sendPushNotificationToUser({ userId, title, body, data = {}, dataOnly = false }) {
    const tokenRes = await db_1.default.query(`SELECT token FROM user_fcm_tokens WHERE user_id = $1`, [userId]);
    const tokens = tokenRes.rows.map(row => row.token);
    if (tokens.length === 0) {
        console.log(`[FCM] No tokens found for user ${userId}`);
        return;
    }
    const messagePayload = {
        tokens,
        data: {
            ...data,
            title,
            body
        },
        android: {
            priority: 'high'
        }
    };
    if (!dataOnly) {
        messagePayload.notification = {
            title,
            body
        };
        messagePayload.android.notification = {
            channelId: 'medisync_notifications',
            priority: 'max',
            defaultSound: true,
            defaultVibrateTimings: true,
            visibility: 'public'
        };
    }
    const response = await firebase_1.firebaseMessaging.sendEachForMulticast(messagePayload);
    const failedTokens = [];
    response.responses.forEach((result, index) => {
        if (!result.success) {
            const errorCode = result.error?.code;
            if (errorCode === 'messaging/registration-token-not-registered' ||
                errorCode === 'messaging/invalid-registration-token') {
                failedTokens.push(tokens[index]);
            }
            console.error('[FCM] Send failed:', result.error);
        }
    });
    if (failedTokens.length > 0) {
        await db_1.default.query(`DELETE FROM user_fcm_tokens WHERE token = ANY($1::text[])`, [failedTokens]);
    }
}
