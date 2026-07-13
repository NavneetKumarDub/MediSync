"use strict";
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.firebaseMessaging = exports.firebaseAuth = void 0;
const firebase_admin_1 = __importDefault(require("firebase-admin"));
const firebase_admin_key_json_1 = __importDefault(require("../../firebase-admin-key.json"));
firebase_admin_1.default.initializeApp({
    credential: firebase_admin_1.default.credential.cert(firebase_admin_key_json_1.default)
});
exports.firebaseAuth = firebase_admin_1.default.auth();
exports.firebaseMessaging = firebase_admin_1.default.messaging();
