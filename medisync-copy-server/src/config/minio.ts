

import {S3Client,CreateBucketCommand,HeadBucketCommand,GetObjectCommand,DeleteObjectCommand,PutObjectCommand} from '@aws-sdk/client-s3'

import {getSignedUrl} from '@aws-sdk/s3-request-presigner'
import dotenv from 'dotenv'
dotenv.config()

const s3Client = new S3Client({
    endpoint: process.env.SUPABASE_S3_ENDPOINT,
    region:process.env.SUPABASE_S3_REGION || 'ap-south-1',
    credentials:{
        accessKeyId: process.env.SUPABASE_S3_ACCESS_KEY_ID!,
        secretAccessKey: process.env.SUPABASE_S3_SECRET_ACCESS_KEY!,
    },
    forcePathStyle: true,
})

export const BUCKETS = {
      PROFILE_PHOTOS: 'profile-photos',
      MEDICAL_RECORDS: 'medical-records',
  }

 export const initMinIO = async () => {
      try {
          for (const bucket of Object.values(BUCKETS)) {
              try {
                  await s3Client.send(new HeadBucketCommand({ Bucket: bucket }))
                  console.log(`Bucket ${bucket} already exists`)
              } catch {
                  await s3Client.send(new CreateBucketCommand({
                        Bucket: bucket 
                    }))
                  console.log(`Bucket ${bucket} created`)
              }
          }
          console.log('Supabase Storage connected ✅')
      } catch (error) {
          console.error('Supabase Storage connection error:', error)
      }
  }
  
 export const getPresignedUploadUrl = async (bucket: string, key:
  string, expiresIn = 900) => {
      return getSignedUrl(s3Client, new PutObjectCommand({ Bucket:
  bucket, Key: key }), { expiresIn })
  }
  
  export const getPresignedDownloadUrl = async (bucket: string, key:
  string, expiresIn = 3600) => {
      return getSignedUrl(s3Client, new GetObjectCommand({ Bucket:
  bucket, Key: key }), { expiresIn })
  }
  
  export const deleteObject = async (bucket: string, key: string) =>
  {
      await s3Client.send(new DeleteObjectCommand({ Bucket: bucket,
  Key: key }))
  }
  
  export default s3Client

