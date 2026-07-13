"use strict";
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
const express_1 = __importDefault(require("express"));
const dotenv_1 = __importDefault(require("dotenv"));
const http_1 = require("http");
const db_1 = __importDefault(require("./src/config/db"));
const appointment_route_1 = __importDefault(require("./src/routes/appointment.route"));
const chat_ws_1 = require("./src/websocket/chat.ws");
const video_ws_1 = require("./src/websocket/video.ws");
const patient_route_1 = __importDefault(require("./src/routes/patient.route"));
const doctor_route_1 = __importDefault(require("./src/routes/doctor.route"));
const slots_route_1 = __importDefault(require("./src/routes/slots.route"));
const user_route_1 = __importDefault(require("./src/routes/user.route"));
const auth_route_1 = __importDefault(require("./src/routes/auth.route"));
const chat_route_1 = __importDefault(require("./src/routes/chat.route"));
const minio_1 = require("./src/config/minio");
const upload_route_1 = __importDefault(require("./src/routes/upload.route"));
const records_route_1 = __importDefault(require("./src/routes/records.route"));
const rating_route_1 = __importDefault(require("./src/routes/rating.route"));
const redis_1 = require("./src/config/redis");
const slot_generator_1 = require("./src/jobs/slot.generator");
dotenv_1.default.config();
const app = (0, express_1.default)();
const server = (0, http_1.createServer)(app);
app.use(express_1.default.json());
app.use('/api/auth', auth_route_1.default);
app.use('/api/user', user_route_1.default);
app.use('/api/slots', slots_route_1.default);
app.use('/api/doctor', doctor_route_1.default);
app.use('/api/patient', patient_route_1.default);
app.use('/api/appointments', appointment_route_1.default);
app.use('/api/chat', chat_route_1.default);
app.use('/api/upload', upload_route_1.default);
app.use('/api/records', records_route_1.default);
app.use('/api/ratings', rating_route_1.default);
app.get('/', (req, res) => {
    res.json({ message: 'MediSync API running' });
});
app.get('/health', async (req, res) => {
    try {
        await db_1.default.query('SELECT 1');
        await redis_1.publisher.ping();
        res.status(200).json({ status: 'ok' });
    }
    catch (err) {
        res.status(503).json({ status: 'error' });
    }
});
db_1.default.query('SELECT NOW()')
    .then(() => {
    (0, minio_1.initMinIO)();
    console.log('Database connected ');
    (0, slot_generator_1.startSlotGeneratorJob)();
})
    .catch((err) => console.log('Database error:', err));
(0, chat_ws_1.initChatWebSocket)(server);
(0, video_ws_1.initVideoWebSocket)(server);
const PORT = Number(process.env.PORT) || 3000;
server.listen(PORT, '0.0.0.0', () => {
    console.log(`Server running on port ${PORT}`);
});
