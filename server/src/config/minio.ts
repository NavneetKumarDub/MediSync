import * as Minio from 'minio'

const minioClient = new Minio.Client({
    endPoint:  process.env.MINIO_ENDPOINT || 'localhost',
    port:      parseInt(process.env.MINIO_PORT || '9000'),
    useSSL:    process.env.MINIO_USE_SSL === 'true',
    accessKey: process.env.MINIO_ACCESS_KEY || 'minioadmin',
    secretKey: process.env.MINIO_SECRET_KEY || 'minioadmin123'
})
export const publicMinioClient = new Minio.Client({
    endPoint:  '192.168.1.8',
    port:      9000,
    useSSL:    false,
    accessKey: process.env.MINIO_ACCESS_KEY || 'minioadmin',
    secretKey: process.env.MINIO_SECRET_KEY || 'minioadmin123'
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