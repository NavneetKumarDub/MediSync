"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
const express_1 = require("express");
const user_controller_1 = require("../controllers/user.controller");
const auth_middleware_1 = require("../middlewares/auth.middleware");
const router = (0, express_1.Router)();
router.post("/register", user_controller_1.registerUser);
router.post("/fcm-token", auth_middleware_1.authMiddleware, user_controller_1.saveFcmToken);
exports.default = router;
