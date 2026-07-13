"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
const express_1 = require("express");
const records_controller_1 = require("../controllers/records.controller");
const auth_middleware_1 = require("../middlewares/auth.middleware");
const router = (0, express_1.Router)();
router.use(auth_middleware_1.authMiddleware);
router.get('/patient', records_controller_1.getPatientRecords);
exports.default = router;
