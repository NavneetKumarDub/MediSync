"use strict";
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.subscriber = exports.publisher = void 0;
const ioredis_1 = __importDefault(require("ioredis"));
const dotenv_1 = __importDefault(require("dotenv"));
dotenv_1.default.config();
const redisUrl = process.env.REDIS_URL || 'redis://localhost:6379';
exports.publisher = new ioredis_1.default(redisUrl);
exports.subscriber = new ioredis_1.default(redisUrl);
exports.publisher.on('connect', () => console.log('Redis publisher connected'));
exports.subscriber.on('connect', () => console.log('Redis subscriber connected'));
exports.publisher.on('error', (err) => console.error('Redis publisher error:', err));
exports.subscriber.on('error', (err) => console.error('Redis subscriber error:', err));
