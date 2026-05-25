import * as Minio from 'minio'

const minioPort = Number(process.env.MINIO_PORT || 9000)
const minioUseSSL = process.env.MINIO_USE_SSL === 'true'
const minioAccessKey = process.env.MINIO_ACCESS_KEY || 'minioadmin'
const minioSecretKey = process.env.MINIO_SECRET_KEY || 'minioadmin123'

const minioClient = new Minio.Client({
    endPoint: process.env.MINIO_ENDPOINT || 'localhost',
    port: minioPort,
    useSSL: minioUseSSL,
    accessKey: minioAccessKey,
    secretKey: minioSecretKey
})

export const publicMinioClient = new Minio.Client({
    endPoint: process.env.MINIO_PUBLIC_HOST || process.env.MINIO_ENDPOINT || 'localhost',
    port: Number(process.env.MINIO_PUBLIC_PORT || process.env.MINIO_PORT || 9000),
    useSSL: (process.env.MINIO_PUBLIC_USE_SSL || process.env.MINIO_USE_SSL) === 'true',
    accessKey: minioAccessKey,
    secretKey: minioSecretKey
})

export const BUCKETS = {
    PROFILE_PHOTOS:  'profile-photos',
    MEDICAL_RECORDS: 'medical-records'
}

export const initMinIO = async () => {
    try {
        for (const bucket of Object.values(BUCKETS)) {
            const exists = await minioClient.bucketExists(bucket)
            if (!exists) {
                await minioClient.makeBucket(bucket)
                console.log(`Bucket ${bucket} created`)
            } else {
                console.log(`Bucket ${bucket} already exists`)
            }
        }
        console.log('MinIO connected ✅')
    } catch (error) {
        console.error('MinIO connection error:', error)
    }
}

export default minioClient
