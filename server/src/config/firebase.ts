import admin from 'firebase-admin'
import serviceAccount from '../../firebase-admin-key.json'

admin.initializeApp({
    credential: admin.credential.cert(serviceAccount as admin.ServiceAccount)
})

export const firebaseAuth = admin.auth()