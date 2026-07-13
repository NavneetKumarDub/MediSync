"use strict";
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.deleteObject = exports.getPresignedDownloadUrl = exports.getPresignedUploadUrl = exports.initMinIO = exports.BUCKETS = void 0;
const client_s3_1 = require("@aws-sdk/client-s3");
const s3_request_presigner_1 = require("@aws-sdk/s3-request-presigner");
const dotenv_1 = __importDefault(require("dotenv"));
dotenv_1.default.config();
const s3Client = new client_s3_1.S3Client({
    endpoint: process.env.SUPABASE_S3_ENDPOINT,
    region: process.env.SUPABASE_S3_REGION || 'ap-south-1',
    credentials: {
        accessKeyId: process.env.SUPABASE_S3_ACCESS_KEY_ID,
        secretAccessKey: process.env.SUPABASE_S3_SECRET_ACCESS_KEY,
    },
    forcePathStyle: true,
});
exports.BUCKETS = {
    PROFILE_PHOTOS: 'profile-photos',
    MEDICAL_RECORDS: 'medical-records',
};
const initMinIO = async () => {
    try {
        for (const bucket of Object.values(exports.BUCKETS)) {
            try {
                await s3Client.send(new client_s3_1.HeadBucketCommand({ Bucket: bucket }));
                console.log(`Bucket ${bucket} already exists`);
            }
            catch {
                await s3Client.send(new client_s3_1.CreateBucketCommand({
                    Bucket: bucket
                }));
                console.log(`Bucket ${bucket} created`);
            }
        }
        console.log('Supabase Storage connected ✅');
    }
    catch (error) {
        console.error('Supabase Storage connection error:', error);
    }
};
exports.initMinIO = initMinIO;
const getPresignedUploadUrl = async (bucket, key, expiresIn = 900) => {
    return (0, s3_request_presigner_1.getSignedUrl)(s3Client, new client_s3_1.PutObjectCommand({ Bucket: bucket, Key: key }), { expiresIn });
};
exports.getPresignedUploadUrl = getPresignedUploadUrl;
const getPresignedDownloadUrl = async (bucket, key, expiresIn = 3600) => {
    return (0, s3_request_presigner_1.getSignedUrl)(s3Client, new client_s3_1.GetObjectCommand({ Bucket: bucket, Key: key }), { expiresIn });
};
exports.getPresignedDownloadUrl = getPresignedDownloadUrl;
const deleteObject = async (bucket, key) => {
    await s3Client.send(new client_s3_1.DeleteObjectCommand({ Bucket: bucket,
        Key: key }));
};
exports.deleteObject = deleteObject;
exports.default = s3Client;
